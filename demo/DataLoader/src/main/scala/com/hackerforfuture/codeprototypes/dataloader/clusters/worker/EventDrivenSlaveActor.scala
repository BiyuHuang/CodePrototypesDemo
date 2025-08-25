/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.clusters.worker

import akka.actor.{ActorPath, ActorRef, Props, Terminated}
import com.hackerforfuture.codeprototypes.dataloader.clusters._
import com.hackerforfuture.codeprototypes.dataloader.events._

import java.time.Instant
import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

/**
 * 事件驱动的Slave Actor
 * 
 * 功能增强：
 * - 发布任务执行相关事件
 * - 订阅任务分配事件
 * - 支持任务状态跟踪
 * - 更好的错误处理和恢复
 */
class EventDrivenSlaveActor(master: ActorRef, override val eventBus: DataLoaderEventBus)
  extends HybridMessageEventActor with EventHandler {

  // Worker的能力集合
  private val capabilities: Set[String] = Set("data-processing", "file-transfer", "computation")
  
  // 当前执行的任务
  private val activeTasks = mutable.HashMap.empty[String, TaskExecution]
  
  // Worker状态
  private var workerStatus: WorkerStatus = WorkerStatus.Initializing

  /**
   * 任务执行信息
   */
  case class TaskExecution(
    taskId: String,
    taskType: String,
    startTime: Instant,
    status: TaskExecutionStatus
  )

  /**
   * 任务执行状态
   */
  sealed trait TaskExecutionStatus
  case object Running extends TaskExecutionStatus
  case object Paused extends TaskExecutionStatus
  case object Completed extends TaskExecutionStatus
  case object Failed extends TaskExecutionStatus

  /**
   * Worker状态
   */
  sealed trait WorkerStatus
  object WorkerStatus {
    case object Initializing extends WorkerStatus
    case object Ready extends WorkerStatus
    case object Busy extends WorkerStatus
    case object Stopping extends WorkerStatus
  }

  override def preStart(): Unit = {
    super.preStart()
    
    // 启动心跳定时器
    import context.dispatcher
    context.system.scheduler.schedule(5.seconds, 10.seconds, self, Heartbeat)
    
    // 发送注册消息
    self ! Register
    
    // 更新状态
    workerStatus = WorkerStatus.Ready
  }

  /**
   * 设置事件订阅
   */
  override protected def setupEventSubscriptions(): Unit = {
    // 订阅任务分配事件
    subscribeToEvent(classOf[TaskAssigned]) { event =>
      if (event.workerId == actorId) {
        handleTaskAssignedEvent(event)
      }
    }
    
    // 订阅系统关闭事件
    subscribeToEvent(classOf[SystemShutdown]) { event =>
      handleSystemShutdownEvent(event)
    }
  }

  /**
   * 消息到事件的转换
   */
  override protected def messageToEvent: PartialFunction[Message, DomainEvent] = {
    case Register =>
      WorkerRegistered(
        workerId = actorId,
        workerPath = self.path,
        capabilities = capabilities
      )
      
    case Heartbeat =>
      HeartbeatReceived(
        workerId = actorId,
        workerPath = self.path
      )
  }

  /**
   * 处理传统消息
   */
  override protected def handleMessage(message: Message): Unit = {
    message match {
      case Heartbeat => handleHeartbeatEvent()
      case Register => handleRegisterEvent()
      case RegisterTimeout => handleRegisterEvent()
      case StopActor => handleStopEvent()
      case _ => log.warning(s"Unknown message: $message")
    }
  }

  /**
   * 处理注册事件
   */
  override def handleRegisterEvent(): Unit = {
    master ! Register
    log.info("Sent registration request to master.")
    context.watch(master)
  }

  /**
   * 处理心跳事件
   */
  override def handleHeartbeatEvent(): Unit = {
    master ! Heartbeat
    log.debug("Sent heartbeat to master.")
  }

  /**
   * 处理停止事件
   */
  override def handleStopEvent(): Unit = {
    log.info("Received StopActor message, shutting down...")
    workerStatus = WorkerStatus.Stopping
    
    // 取消所有正在执行的任务
    activeTasks.values.foreach { taskExecution =>
      publishEvent(TaskCancelled(
        taskId = taskExecution.taskId,
        reason = "Worker shutdown",
        cancelledBy = actorId
      ))
    }
    activeTasks.clear()
    
    context.stop(self)
  }

  /**
   * 处理Master终止
   */
  private def handleMasterTerminated(master: ActorRef): Unit = {
    log.warning(s"Master ${master.path.name} terminated. ${self.path.name} is shutting down...")
    self ! StopActor
  }

  /**
   * 完整的消息处理器
   */
  override def receive: Receive = {
    case Terminated(actorRef) if actorRef == master => 
      handleMasterTerminated(actorRef)
    case msg: Message => 
      handleMessage(msg)
      messageToEvent.lift(msg).foreach(publishEventAsync)
    case taskRequest: TaskRequest =>
      handleTaskRequest(taskRequest)
    case other => 
      log.warning(s"Unhandled message: $other")
  }

  // ========== 事件处理器 ==========

  /**
   * 处理任务分配事件
   */
  private def handleTaskAssignedEvent(event: TaskAssigned): Unit = {
    log.info(s"Received task assignment: ${event.taskId}")
    
    if (activeTasks.size >= getMaxConcurrentTasks) {
      log.warning(s"Worker is at capacity, rejecting task ${event.taskId}")
      publishEvent(TaskFailed(
        taskId = event.taskId,
        workerId = actorId,
        error = "Worker at capacity",
        correlationId = event.correlationId
      ))
      return
    }

    // 创建任务执行记录
    val taskExecution = TaskExecution(
      taskId = event.taskId,
      taskType = "unknown", // 从任务详情中获取
      startTime = Instant.now(),
      status = Running
    )
    activeTasks.put(event.taskId, taskExecution)
    
    // 更新状态
    workerStatus = if (activeTasks.size == getMaxConcurrentTasks) WorkerStatus.Busy else WorkerStatus.Ready
    
    // 发布任务开始事件
    publishEvent(TaskStarted(
      taskId = event.taskId,
      workerId = actorId,
      correlationId = event.correlationId
    ))
    
    // 开始执行任务
    executeTask(event.taskId)
  }

  /**
   * 处理系统关闭事件
   */
  private def handleSystemShutdownEvent(event: SystemShutdown): Unit = {
    if (event.systemId == "master" || event.systemId.contains("system")) {
      log.info("System shutdown event received, preparing to stop")
      self ! StopActor
    }
  }

  /**
   * 执行任务
   */
  private def executeTask(taskId: String): Unit = {
    import context.dispatcher
    
    // 模拟任务执行
    val taskFuture = scala.concurrent.Future {
      // 模拟工作
      Thread.sleep(scala.util.Random.nextInt(5000) + 1000) // 1-6秒
      
      // 随机成功或失败
      if (scala.util.Random.nextDouble() > 0.2) { // 80%成功率
        SuccessResult("Task completed successfully")
      } else {
        throw new RuntimeException("Task execution failed")
      }
    }
    
    taskFuture.onComplete {
      case Success(result) =>
        handleTaskSuccess(taskId, result)
      case Failure(exception) =>
        handleTaskFailure(taskId, exception)
    }
  }

  /**
   * 处理任务成功
   */
  private def handleTaskSuccess(taskId: String, result: TaskResult): Unit = {
    activeTasks.get(taskId).foreach { taskExecution =>
      activeTasks.put(taskId, taskExecution.copy(status = Completed))
      
      publishEvent(TaskCompleted(
        taskId = taskId,
        workerId = actorId,
        result = result
      ))
      
      // 清理任务
      activeTasks.remove(taskId)
      updateWorkerStatus()
      
      log.info(s"Task $taskId completed successfully")
    }
  }

  /**
   * 处理任务失败
   */
  private def handleTaskFailure(taskId: String, exception: Throwable): Unit = {
    activeTasks.get(taskId).foreach { taskExecution =>
      activeTasks.put(taskId, taskExecution.copy(status = Failed))
      
      publishEvent(TaskFailed(
        taskId = taskId,
        workerId = actorId,
        error = exception.getMessage
      ))
      
      // 清理任务
      activeTasks.remove(taskId)
      updateWorkerStatus()
      
      log.error(exception, s"Task $taskId failed")
    }
  }

  /**
   * 处理任务请求
   */
  private def handleTaskRequest(request: TaskRequest): Unit = {
    log.info(s"Received task request: ${request.taskId}")
    
    // 发布任务提交事件
    publishEvent(TaskSubmitted(
      taskId = request.taskId,
      taskType = request.taskType,
      priority = request.priority,
      submittedBy = actorId
    ))
  }

  /**
   * 更新Worker状态
   */
  private def updateWorkerStatus(): Unit = {
    workerStatus = if (activeTasks.isEmpty) {
      WorkerStatus.Ready
    } else if (activeTasks.size >= getMaxConcurrentTasks) {
      WorkerStatus.Busy
    } else {
      WorkerStatus.Ready
    }
  }

  /**
   * 获取最大并发任务数
   */
  private def getMaxConcurrentTasks: Int = 3

  /**
   * 获取Worker状态信息
   */
  def getWorkerStatus: WorkerStatusInfo = {
    WorkerStatusInfo(
      workerId = actorId,
      status = workerStatus.toString,
      activeTasks = activeTasks.size,
      maxTasks = getMaxConcurrentTasks,
      capabilities = capabilities,
      uptime = System.currentTimeMillis() - context.system.startTime
    )
  }

  override def handleRegisterTimeout(): Unit = {
    log.warning("Registration timeout, retrying...")
    handleRegisterEvent()
  }

  override def handelCheckHeartbeatEvent(): Unit = {
    // Slave不需要检查心跳
  }
}

/**
 * 任务请求消息
 */
case class TaskRequest(
  taskId: String,
  taskType: String,
  priority: Int,
  data: Any = None
)

/**
 * Worker状态信息
 */
case class WorkerStatusInfo(
  workerId: String,
  status: String,
  activeTasks: Int,
  maxTasks: Int,
  capabilities: Set[String],
  uptime: Long
)

/**
 * 伴生对象
 */
object EventDrivenSlaveActor {
  def props(master: ActorRef, eventBus: DataLoaderEventBus): Props = 
    Props(new EventDrivenSlaveActor(master, eventBus))
}
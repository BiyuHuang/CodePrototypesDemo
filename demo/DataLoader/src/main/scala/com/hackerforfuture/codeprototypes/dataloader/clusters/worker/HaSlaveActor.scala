/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.clusters.worker

import akka.actor.{ActorRef, ActorSelection, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.MemberStatus
import akka.pattern.ask
import akka.util.Timeout
import com.hackerforfuture.codeprototypes.dataloader.clusters.{Message, Register, DiscoverMaster, RegisteredConfirmation, WorkerNotRegistered, HeartBeat, HeartBeatAck, AssignTask, TaskExecution, CustomMessage}
import com.hackerforfuture.codeprototypes.dataloader.clusters.master.{GetClusterStatus, HaClusterStatus}
import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport
import com.hackerforfuture.codeprototypes.dataloader.events._

import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

/**
 * 高可用SlaveActor - 支持Master故障转移和自动重连
 * 
 * 特性：
 * 1. 自动发现Master节点
 * 2. Master故障检测和重连
 * 3. 任务状态保持
 * 4. 优雅的状态迁移
 */
class HaSlaveActor(
  workerId: String,
  capacity: Int = 10,
  private val bus: DataLoaderEventBus
)(implicit ec: ExecutionContext) extends EventDrivenActor with LogSupport {

  // 实现EventDrivenActor需要的eventBus抽象方法
  override def eventBus: DataLoaderEventBus = bus

  // 集群相关
  private val cluster = Cluster(context.system)
  private val nodeId = s"${cluster.selfAddress.toString}-$workerId"
  
  // Master连接状态
  private val currentMaster = new AtomicReference[Option[ActorRef]](None)
  private var masterSelection: Option[ActorSelection] = None
  private var lastMasterHeartbeat = Instant.now()
  private var connectionAttempts = 0
  private val maxConnectionAttempts = 5
  
  // Worker状态
  private val currentTasks = mutable.Map[String, TaskExecutionState]()
  private var workerStatus = "INITIALIZING"
  private val workCapacity = capacity
  
  // 调度器 (使用Akka 2.5兼容的API)
  private val heartbeatScheduler = context.system.scheduler
    .schedule(5.seconds, 10.seconds, self, SendHeartbeat)(ec)
  
  private val masterDiscoveryScheduler = context.system.scheduler
    .schedule(2.seconds, 15.seconds, self, DiscoverMaster)(ec)
  
  private val taskStatusScheduler = context.system.scheduler
    .schedule(30.seconds, 30.seconds, self, ReportTaskStatus)(ec)

  implicit val timeout: Timeout = Timeout(10.seconds)

  override def preStart(): Unit = {
    super.preStart()
    logger.info(s"Starting HA SlaveActor: $workerId on node: $nodeId")
    
    // 订阅集群事件
    cluster.subscribe(self, classOf[MemberEvent], classOf[LeaderChanged])
    
    // 订阅Master选举事件
    subscribeToMasterEvents()
    
    // 开始发现Master
    self ! DiscoverMaster
    
    workerStatus = "READY"
    logger.info(s"HA SlaveActor started: $workerId with capacity: $capacity")
  }

  override def postStop(): Unit = {
    logger.info(s"Stopping HA SlaveActor: $workerId")
    
    heartbeatScheduler.cancel()
    masterDiscoveryScheduler.cancel()
    taskStatusScheduler.cancel()
    cluster.unsubscribe(self)
    
    // 通知Master Worker下线
    currentMaster.get().foreach { master =>
      master ! WorkerShutdown(workerId)
    }
    
    super.postStop()
  }

  override def receive: Receive = {
    // 集群事件
    case memberEvent: MemberEvent =>
      handleClusterEvent(memberEvent)
    
    case LeaderChanged(leader) =>
      handleLeaderChanged(leader)
    
    // Master发现和连接
    case DiscoverMaster =>
      discoverMaster()
    
    case MasterDiscovered(masterRef) =>
      connectToMaster(masterRef)
    
    case RegisteredConfirmation(id) =>
      handleRegistrationConfirmed(id)
    
    case WorkerNotRegistered(id) =>
      handleRegistrationFailed(id)
    
    // 心跳相关
    case SendHeartbeat =>
      sendHeartbeat()
    
    case HeartBeatAck(id) =>
      handleHeartbeatAck(id)
    
    case ReportTaskStatus =>
      reportTaskStatus()
    
    // 任务处理
    case assignTask: AssignTask =>
      handleTaskAssignment(assignTask)
    
    case taskResult: TaskResult =>
      handleTaskResult(taskResult)
    
    // 状态查询
    case GetWorkerStatus =>
      sender() ! WorkerStatusResponse(
        workerId = workerId,
        status = workerStatus,
        capacity = workCapacity,
        currentTasks = currentTasks.size,
        masterConnected = currentMaster.get().isDefined
      )
    
    // Master连接状态
    case MasterConnectionLost =>
      handleMasterConnectionLost()
    
    case MasterReconnected(masterRef) =>
      handleMasterReconnected(masterRef)
    
    case _ =>
      logger.debug(s"Worker $workerId received unhandled message")
  }
  
  /**
   * 发现Master节点
   */
  private def discoverMaster(): Unit = {
    if (currentMaster.get().isDefined) {
      return // 已连接到Master
    }
    
    logger.debug(s"Worker $workerId discovering master nodes...")
    
    // 查找集群中的Master节点
    val masterNodes = cluster.state.members
      .filter(_.status == MemberStatus.Up)
      .filter(_.roles.contains("master"))
      .toSeq
    
    if (masterNodes.nonEmpty) {
      // 随机选择一个Master节点尝试连接
      val selectedMaster = masterNodes(Random.nextInt(masterNodes.size))
      val masterPath = s"${selectedMaster.address}/user/ha-master"
      
      logger.info(s"Worker $workerId attempting to connect to master: $masterPath")
      
      val masterSelection = context.actorSelection(masterPath)
      this.masterSelection = Some(masterSelection)
      
      // 查询Master状态
      masterSelection.resolveOne(5.seconds).onComplete {
        case Success(masterRef) =>
          self ! MasterDiscovered(masterRef)
        case Failure(ex) =>
          logger.warn(s"Failed to resolve master: $masterPath", ex)
          connectionAttempts += 1
          if (connectionAttempts < maxConnectionAttempts) {
            context.system.scheduler.scheduleOnce(5.seconds, self, DiscoverMaster)
          }
      }
    } else {
      logger.warn(s"Worker $workerId: No master nodes found in cluster")
      connectionAttempts += 1
      if (connectionAttempts < maxConnectionAttempts) {
        context.system.scheduler.scheduleOnce(10.seconds, self, DiscoverMaster)
      }
    }
  }
  
  /**
   * 连接到Master
   */
  private def connectToMaster(masterRef: ActorRef): Unit = {
    logger.info(s"Worker $workerId connecting to master: ${masterRef.path}")
    
    // 首先检查Master是否是Leader
    (masterRef ? GetClusterStatus).mapTo[com.hackerforfuture.codeprototypes.dataloader.clusters.master.HaClusterStatus].onComplete {
      case Success(status) if status.isLeader =>
        // Master是Leader，可以注册
        registerWithMaster(masterRef)
      case Success(status) =>
        // Master不是Leader，查找真正的Leader
        logger.info(s"Master ${masterRef.path} is not leader, current leader: ${status.currentLeader}")
        findAndConnectToLeader(status.currentLeader)
      case Failure(ex) =>
        logger.warn(s"Failed to get cluster status from master: ${masterRef.path}", ex)
        connectionAttempts += 1
        scheduleRetryConnection()
    }
  }
  
  /**
   * 查找并连接到Leader
   */
  private def findAndConnectToLeader(leaderOption: Option[String]): Unit = {
    leaderOption match {
      case Some(leaderId) =>
        // 尝试连接到指定的Leader
        val leaderPath = s"$leaderId/user/ha-master"
        val leaderSelection = context.actorSelection(leaderPath)
        
        leaderSelection.resolveOne(5.seconds).onComplete {
          case Success(leaderRef) =>
            registerWithMaster(leaderRef)
          case Failure(ex) =>
            logger.warn(s"Failed to connect to leader: $leaderPath", ex)
            scheduleRetryConnection()
        }
      case None =>
        logger.warn("No leader available, retrying discovery")
        scheduleRetryConnection()
    }
  }
  
  /**
   * 向Master注册
   */
  private def registerWithMaster(masterRef: ActorRef): Unit = {
    logger.info(s"Worker $workerId registering with master: ${masterRef.path}")
    
    val registration = Register(
      workerId = workerId,
      capacity = Some(workCapacity),
      metadata = Map(
        "nodeId" -> nodeId,
        "version" -> "1.1.0",
        "supportedTaskTypes" -> "data-scan,data-upload,data-download"
      )
    )
    
    masterRef ! registration
    connectionAttempts = 0 // 重置连接尝试次数
  }
  
  /**
   * 处理注册确认
   */
  private def handleRegistrationConfirmed(id: String): Unit = {
    if (id == workerId) {
      currentMaster.set(Some(sender()))
      lastMasterHeartbeat = Instant.now()
      workerStatus = "ACTIVE"
      
      logger.info(s"Worker $workerId successfully registered with master: ${sender().path}")
      
      // 发布Worker注册事件
      publishEvent(WorkerRegistered(
        workerId = workerId,
        workerPath = self.path,
        capabilities = Set(s"capacity:$workCapacity")
      ))
      
      // 如果有正在执行的任务，报告状态
      if (currentTasks.nonEmpty) {
        reportTaskStatus()
      }
    }
  }
  
  /**
   * 处理注册失败
   */
  private def handleRegistrationFailed(id: String): Unit = {
    if (id == workerId) {
      logger.warn(s"Worker $workerId registration failed")
      currentMaster.set(None)
      workerStatus = "DISCONNECTED"
      
      scheduleRetryConnection()
    }
  }
  
  /**
   * 发送心跳
   */
  private def sendHeartbeat(): Unit = {
    currentMaster.get() match {
      case Some(master) =>
        val heartbeat = HeartBeat(
          workerId = workerId,
          status = Some(workerStatus),
          metrics = Some(Map(
            "activeTasks" -> currentTasks.size.toString,
            "capacity" -> workCapacity.toString,
            "cpuUsage" -> "0.5", // 模拟CPU使用率
            "memoryUsage" -> "0.3" // 模拟内存使用率
          ))
        )
        
        master ! heartbeat
        
        // 检查心跳超时
        val now = Instant.now()
        if (now.isAfter(lastMasterHeartbeat.plusSeconds(60))) {
          logger.warn(s"Worker $workerId: Master heartbeat timeout")
          self ! MasterConnectionLost
        }
        
      case None =>
        logger.debug(s"Worker $workerId: No master connected, skipping heartbeat")
    }
  }
  
  /**
   * 处理心跳确认
   */
  private def handleHeartbeatAck(id: String): Unit = {
    if (id == workerId) {
      lastMasterHeartbeat = Instant.now()
      logger.debug(s"Worker $workerId: Heartbeat acknowledged")
    }
  }
  
  /**
   * 处理任务分配
   */
  private def handleTaskAssignment(assignTask: AssignTask): Unit = {
    if (currentTasks.size >= workCapacity) {
      logger.warn(s"Worker $workerId: Capacity exceeded, rejecting task: ${assignTask.taskId}")
      sender() ! TaskRejected(assignTask.taskId, "Capacity exceeded")
      return
    }
    
    val taskExecution = TaskExecutionState(
      taskId = assignTask.taskId,
      taskType = assignTask.taskType,
      payload = assignTask.payload,
      startTime = Instant.now(),
      status = "ASSIGNED"
    )
    
    currentTasks += assignTask.taskId -> taskExecution
    
    logger.info(s"Worker $workerId: Task assigned: ${assignTask.taskId}")
    
    // 发布任务开始事件
    publishEvent(TaskStarted(
      taskId = assignTask.taskId,
      workerId = workerId,
      startedAt = Instant.now()
    ))
    
    // 异步执行任务
    executeTask(taskExecution)
    
    sender() ! TaskAccepted(assignTask.taskId)
  }
  
  /**
   * 执行任务
   */
  private def executeTask(taskExecution: TaskExecutionState): Unit = {
    Future {
      try {
        logger.info(s"Worker $workerId: Executing task: ${taskExecution.taskId}")
        
        // 更新任务状态
        val updatedTask = taskExecution.copy(status = "RUNNING")
        currentTasks += taskExecution.taskId -> updatedTask
        
        // 模拟任务执行
        val executionTime = Random.nextInt(10) + 5 // 5-15秒
        Thread.sleep(executionTime * 1000)
        
        // 任务完成
        val result = Map(
          "status" -> "completed",
          "executionTime" -> executionTime.toString,
          "result" -> s"Task ${taskExecution.taskId} completed successfully"
        )
        
        self ! TaskResult(taskExecution.taskId, success = true, result)
        
      } catch {
        case ex: Exception =>
          logger.error(s"Worker $workerId: Task execution failed: ${taskExecution.taskId}", ex)
          val errorResult = Map(
            "status" -> "failed",
            "error" -> ex.getMessage
          )
          self ! TaskResult(taskExecution.taskId, success = false, errorResult)
      }
    }
  }
  
  /**
   * 处理任务结果
   */
  private def handleTaskResult(taskResult: TaskResult): Unit = {
    currentTasks.get(taskResult.taskId) match {
      case Some(task) =>
        currentTasks -= taskResult.taskId
        
        if (taskResult.success) {
          logger.info(s"Worker $workerId: Task completed successfully: ${taskResult.taskId}")
          
          // 通知Master任务完成
          currentMaster.get().foreach { master =>
            master ! CustomMessage(
              messageType = "task_completed",
              data = Map(
                "taskId" -> taskResult.taskId,
                "workerId" -> workerId,
                "result" -> taskResult.result.getOrElse("status", "completed")
              )
            )
          }
          
          // 发布任务完成事件
          publishEvent(TaskCompleted(
            taskId = taskResult.taskId,
            workerId = workerId,
            result = SuccessResult(taskResult.result.getOrElse("result", "completed")),
            completedAt = Instant.now()
          ))
          
        } else {
          logger.error(s"Worker $workerId: Task failed: ${taskResult.taskId}")
          
          // 通知Master任务失败
          currentMaster.get().foreach { master =>
            master ! CustomMessage(
              messageType = "task_failed",
              data = Map(
                "taskId" -> taskResult.taskId,
                "workerId" -> workerId,
                "error" -> taskResult.result.getOrElse("error", "unknown error")
              )
            )
          }
          
          // 发布任务失败事件
          publishEvent(TaskFailed(
            taskId = taskResult.taskId,
            workerId = workerId,
            error = taskResult.result.getOrElse("error", "unknown error"),
            failedAt = Instant.now()
          ))
        }
        
      case None =>
        logger.warn(s"Worker $workerId: Received result for unknown task: ${taskResult.taskId}")
    }
  }
  
  /**
   * 报告任务状态
   */
  private def reportTaskStatus(): Unit = {
    currentMaster.get().foreach { master =>
      val taskStatuses = currentTasks.map { case (taskId, task) =>
        taskId -> Map(
          "status" -> task.status,
          "startTime" -> task.startTime.toString,
          "taskType" -> task.taskType
        )
      }.toMap
      
      master ! WorkerTaskStatus(workerId, taskStatuses)
    }
  }
  
  /**
   * 处理Master连接丢失
   */
  private def handleMasterConnectionLost(): Unit = {
    logger.warn(s"Worker $workerId: Master connection lost")
    currentMaster.set(None)
    workerStatus = "DISCONNECTED"
    
    // 发布连接丢失事件
    publishEvent(WorkerDisconnected(
      workerId = workerId,
      reason = "Master connection lost"
    ))
    
    // 开始重新发现Master
    connectionAttempts = 0
    self ! DiscoverMaster
  }
  
  /**
   * 处理Master重连
   */
  private def handleMasterReconnected(masterRef: ActorRef): Unit = {
    logger.info(s"Worker $workerId: Reconnected to master: ${masterRef.path}")
    currentMaster.set(Some(masterRef))
    workerStatus = "ACTIVE"
    lastMasterHeartbeat = Instant.now()
    
    // 重新注册
    registerWithMaster(masterRef)
    
    // 发布重连事件
    publishEvent(WorkerReconnected(
      workerId = workerId
    ))
  }
  
  /**
   * 处理集群事件
   */
  private def handleClusterEvent(event: MemberEvent): Unit = {
    event match {
      case MemberUp(member) =>
        if (member.roles.contains("master")) {
          logger.info(s"Worker $workerId: New master node up: ${member.address}")
          // 如果当前没有连接到Master，尝试连接新的Master
          if (currentMaster.get().isEmpty) {
            self ! DiscoverMaster
          }
        }
      
      case MemberRemoved(member, _) =>
        if (member.roles.contains("master")) {
          logger.info(s"Worker $workerId: Master node removed: ${member.address}")
          // 如果当前连接的Master离开了，重新发现
          currentMaster.get() match {
            case Some(master) if master.path.address == member.address =>
              self ! MasterConnectionLost
            case _ =>
              // 不是当前连接的Master
          }
        }
      
      case _ =>
        logger.debug(s"Worker $workerId: Unhandled cluster event: $event")
    }
  }
  
  /**
   * 处理Leader变更
   */
  private def handleLeaderChanged(leader: Option[akka.actor.Address]): Unit = {
    logger.info(s"Worker $workerId: Cluster leader changed to: $leader")
    // 这里主要用于监控，实际的Master选举通过事件处理
  }
  
  /**
   * 订阅Master事件
   */
  private def subscribeToMasterEvents(): Unit = {
    subscribeToEvent(classOf[LeaderElected]) { event =>
      logger.info(s"Worker $workerId: New leader elected: ${event.leaderId}")
      // 如果当前没有连接或连接的不是新Leader，尝试连接新Leader
      currentMaster.get() match {
        case None =>
          self ! DiscoverMaster
        case Some(master) if !master.path.toString.contains(event.leaderId) =>
          logger.info(s"Worker $workerId: Switching to new leader: ${event.leaderId}")
          currentMaster.set(None)
          self ! DiscoverMaster
        case _ =>
          // 已经连接到正确的Leader
      }
    }
    
    subscribeToEvent(classOf[LeaderStepDown]) { event =>
      logger.info(s"Worker $workerId: Leader stepped down: ${event.leaderId}")
      // 如果连接的Master不再是Leader，断开连接并重新发现
      currentMaster.get() match {
        case Some(master) if master.path.toString.contains(event.leaderId) =>
          logger.info(s"Worker $workerId: Connected master stepped down, reconnecting")
          self ! MasterConnectionLost
        case _ =>
          // 不是当前连接的Master
      }
    }
  }
  
  /**
   * 调度重连
   */
  private def scheduleRetryConnection(): Unit = {
    if (connectionAttempts < maxConnectionAttempts) {
      val delay = math.min(connectionAttempts * 2, 30).seconds
      logger.info(s"Worker $workerId: Scheduling reconnection attempt ${connectionAttempts + 1} in $delay")
      context.system.scheduler.scheduleOnce(delay, self, DiscoverMaster)
    } else {
      logger.error(s"Worker $workerId: Max connection attempts reached, giving up")
      workerStatus = "FAILED"
    }
  }
}

/**
 * HaSlaveActor的伴生对象
 */
object HaSlaveActor {
  def props(workerId: String, capacity: Int, eventBus: DataLoaderEventBus)
           (implicit ec: ExecutionContext): Props = {
    Props(new HaSlaveActor(workerId, capacity, eventBus))
  }
}

// ========== 消息定义 ==========

case class MasterDiscovered(masterRef: ActorRef)
case object SendHeartbeat
case object ReportTaskStatus
case object MasterConnectionLost
case class MasterReconnected(masterRef: ActorRef)
case object GetWorkerStatus

case class TaskResult(taskId: String, success: Boolean, result: Map[String, String])
case class TaskAccepted(taskId: String)
case class TaskRejected(taskId: String, reason: String)
case class WorkerTaskStatus(workerId: String, taskStatuses: Map[String, Map[String, String]])
case class WorkerShutdown(workerId: String)

// ========== 响应消息 ==========

case class WorkerStatusResponse(
  workerId: String,
  status: String,
  capacity: Int,
  currentTasks: Int,
  masterConnected: Boolean
)

// ========== 内部状态类 ==========

// 内部任务执行状态 
case class TaskExecutionState(
  taskId: String,
  taskType: String,
  payload: Map[String, Any],
  startTime: Instant,
  status: String
)
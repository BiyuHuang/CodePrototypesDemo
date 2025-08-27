/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.clusters.master

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import com.hackerforfuture.codeprototypes.dataloader.clusters._
import com.hackerforfuture.codeprototypes.dataloader.clusters.Message
import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport
import com.hackerforfuture.codeprototypes.dataloader.events._

import java.time.Instant
import java.util.UUID
import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * 高可用MasterActor - 集成集群管理和Leader选举
 * 
 * 特性：
 * 1. 自动Leader选举
 * 2. 故障转移
 * 3. 状态迁移
 * 4. Worker重连支持
 */
class HaMasterActor(
  nodeId: String,
  private val bus: DataLoaderEventBus
)(implicit ec: ExecutionContext) extends EventDrivenActor with LogSupport {

  // 实现EventDrivenActor需要的eventBus抽象方法
  override def eventBus: DataLoaderEventBus = bus

  // 集群管理
  private val cluster = Cluster(context.system)
  private val clusterManager = new ClusterManager(nodeId, cluster, bus)
  
  // Master状态
  private val registeredWorkers = mutable.Map[String, RegisteredWorker]()
  private val activeTasks = mutable.Map[String, ActiveTask]()
  private val taskQueue = mutable.Queue[PendingTask]()
  
  // 性能监控
  private var lastHeartbeatCheck = Instant.now()
  private val heartbeatTimeout = 30.seconds
  
  // 调度器 (使用Akka 2.5兼容的API)
  private val heartbeatScheduler = context.system.scheduler
    .schedule(10.seconds, 10.seconds, self, CheckHeartbeats)(ec)
  
  // 任务分配调度器
  private val taskScheduler = context.system.scheduler
    .schedule(5.seconds, 5.seconds, self, ProcessTaskQueue)(ec)

  override def preStart(): Unit = {
    super.preStart()
    logger.info(s"Starting HA MasterActor for node: $nodeId")
    
    // 启动集群管理器
    clusterManager.start()
    
    // 订阅集群事件
    subscribeToClusterEvents()
    
    // 订阅任务相关事件
    subscribeToTaskEvents()
    
    logger.info(s"HA MasterActor started, isLeader: ${clusterManager.isLeader}")
  }

  override def postStop(): Unit = {
    logger.info(s"Stopping HA MasterActor for node: $nodeId")
    
    heartbeatScheduler.cancel()
    taskScheduler.cancel()
    clusterManager.stop()
    
    super.postStop()
  }

  override def receive: Receive = {
    // 集群管理消息
    case clusterEvent: akka.cluster.ClusterEvent.ClusterDomainEvent =>
      clusterManager.handleClusterEvent(clusterEvent)
    
    // Leader选举消息
    case VoteRequest(term, candidateId) =>
      val granted = clusterManager.handleVoteRequest(term, candidateId)
      sender() ! VoteResponse(term, nodeId, candidateId, granted)
    
    case VoteResponse(term, voterId, candidateId, granted) =>
      clusterManager.handleVoteResult(term, voterId, candidateId, granted)
    
    case HeartbeatMessage(term, leaderId) =>
      clusterManager.handleHeartbeat(term, leaderId)
    
    // Master业务消息（只有Leader处理）
    case msg if clusterManager.isLeader =>
      handleLeaderMessage(msg)
    
    // Follower只处理状态查询
    case msg =>
      handleFollowerMessage(msg)
  }
  
  /**
   * 处理Leader专有消息
   */
  private def handleLeaderMessage(msg: Any): Unit = msg match {
    case register: Register =>
      handleWorkerRegister(register)
    
    case heartbeat: HeartBeat =>
      handleWorkerHeartbeat(heartbeat)
    
    case customMsg: CustomMessage =>
      handleCustomMessage(customMsg)
    
    case taskSubmit: SubmitTask =>
      handleTaskSubmit(taskSubmit)
    
    case CheckHeartbeats =>
      checkWorkerHeartbeats()
    
    case ProcessTaskQueue =>
      processTaskQueue()
    
    case MigrateState(newLeaderId) =>
      migrateStateToNewLeader(newLeaderId)
    
    case _ =>
      logger.debug(s"Leader received unhandled message: $msg")
  }
  
  /**
   * 处理Follower消息
   */
  private def handleFollowerMessage(msg: Any): Unit = msg match {
    case GetClusterStatus =>
      sender() ! HaClusterStatus(
        isLeader = clusterManager.isLeader,
        currentLeader = clusterManager.getCurrentLeader,
        term = clusterManager.getCurrentTerm,
        clusterSize = clusterManager.getClusterSize
      )
    
    case GetWorkerStates =>
      // Follower返回空状态或最后已知状态
      sender() ! WorkerStates(Map.empty)
    
    case _ =>
      logger.debug(s"Follower ignoring message: $msg")
      sender() ! NotLeaderError(clusterManager.getCurrentLeader)
  }
  
  /**
   * 处理Worker注册
   */
  private def handleWorkerRegister(register: Register): Unit = {
    val workerId = register.workerId
    val workerCapacity = register.capacity.getOrElse(10)
    val worker = RegisteredWorker(
      workerId = workerId,
      workerRef = sender(),
      capacity = workerCapacity,
      currentTasks = mutable.Set.empty,
      lastHeartbeat = Instant.now(),
      status = "ACTIVE"
    )
    
    registeredWorkers += workerId -> worker
    
    // 发布事件
    publishEvent(WorkerRegistered(
      workerId = workerId,
      workerPath = sender().path,
      capabilities = Set(s"capacity:$workerCapacity")
    ))
    
    sender() ! RegisteredConfirmation(workerId)
    logger.info(s"Worker registered: $workerId with capacity: ${worker.capacity}")
  }
  
  /**
   * 处理Worker心跳
   */
  private def handleWorkerHeartbeat(heartbeat: HeartBeat): Unit = {
    val workerId = heartbeat.workerId
    
    registeredWorkers.get(workerId) match {
      case Some(worker) =>
        val newLastHeartbeat = Instant.now()
        val updatedWorker = worker.copy(
          lastHeartbeat = newLastHeartbeat,
          status = heartbeat.status.getOrElse("ACTIVE")
        )
        registeredWorkers += workerId -> updatedWorker
        
        // 发布心跳事件
        publishEvent(HeartbeatReceived(
          workerId = workerId,
          workerPath = sender().path
        ))
        
        sender() ! HeartBeatAck(workerId)
        logger.debug(s"Heartbeat received from worker: $workerId")
        
      case None =>
        logger.warn(s"Heartbeat from unregistered worker: $workerId")
        sender() ! WorkerNotRegistered(workerId)
    }
  }
  
  /**
   * 处理任务提交
   */
  private def handleTaskSubmit(submit: SubmitTask): Unit = {
    val taskId = UUID.randomUUID().toString
    val task = PendingTask(
      taskId = taskId,
      taskType = submit.taskType,
      priority = submit.priority,
      payload = submit.payload,
      submittedAt = Instant.now()
    )
    
    taskQueue.enqueue(task)
    
    // 发布任务提交事件
    publishEvent(TaskSubmitted(
      taskId = taskId,
      taskType = submit.taskType,
      priority = submit.priority,
      submittedBy = "master"
    ))
    
    sender() ! TaskSubmitted(taskId, submit.taskType, submit.priority, "master")
    logger.info(s"Task submitted: $taskId, type: ${submit.taskType}")
  }
  
  /**
   * 处理自定义消息
   */
  private def handleCustomMessage(customMsg: CustomMessage): Unit = {
    customMsg.messageType match {
      case "task_completed" =>
        handleTaskCompleted(customMsg)
      case "task_failed" =>
        handleTaskFailed(customMsg)
      case _ =>
        logger.debug(s"Unhandled custom message type: ${customMsg.messageType}")
    }
  }
  
  /**
   * 处理任务完成
   */
  private def handleTaskCompleted(msg: CustomMessage): Unit = {
    val taskId = msg.data.getOrElse("taskId", "unknown")
    val workerId = msg.data.getOrElse("workerId", "unknown")
    
    // 从活动任务中移除
    activeTasks.remove(taskId)
    
    // 更新Worker状态
    registeredWorkers.get(workerId).foreach { worker =>
      worker.currentTasks -= taskId
    }
    
    // 发布任务完成事件
    publishEvent(TaskCompleted(
      taskId = taskId,
      workerId = workerId,
      result = SuccessResult(msg.data.getOrElse("result", "completed")),
      completedAt = Instant.now()
    ))
    
    logger.info(s"Task completed: $taskId by worker: $workerId")
  }
  
  /**
   * 处理任务失败
   */
  private def handleTaskFailed(msg: CustomMessage): Unit = {
    val taskId = msg.data.getOrElse("taskId", "unknown")
    val workerId = msg.data.getOrElse("workerId", "unknown")
    val error = msg.data.getOrElse("error", "unknown error")
    
    // 从活动任务中移除
    activeTasks.remove(taskId)
    
    // 更新Worker状态
    registeredWorkers.get(workerId).foreach { worker =>
      worker.currentTasks -= taskId
    }
    
    // 发布任务失败事件
    publishEvent(TaskFailed(
      taskId = taskId,
      workerId = workerId,
      error = error,
      failedAt = Instant.now(),
      retryCount = 0
    ))
    
    logger.warn(s"Task failed: $taskId by worker: $workerId, error: $error")
  }
  
  /**
   * 检查Worker心跳超时
   */
  private def checkWorkerHeartbeats(): Unit = {
    if (!clusterManager.isLeader) return
    
    val now = Instant.now()
    val timeoutThreshold = now.minusSeconds(heartbeatTimeout.toSeconds)
    
    val timedOutWorkers = registeredWorkers.filter { case (_, worker) =>
      worker.lastHeartbeat.isBefore(timeoutThreshold)
    }
    
    timedOutWorkers.foreach { case (workerId, worker) =>
      logger.warn(s"Worker heartbeat timeout: $workerId")
      
      // 发布超时事件
      publishEvent(WorkerTimedOut(
        workerId = workerId,
        workerPath = worker.workerRef.path,
        lastHeartbeat = worker.lastHeartbeat
      ))
      
      // 重新分配该Worker的任务
      reassignWorkerTasks(workerId)
      
      // 移除超时Worker
      registeredWorkers -= workerId
    }
  }
  
  /**
   * 处理任务队列
   */
  private def processTaskQueue(): Unit = {
    if (!clusterManager.isLeader || taskQueue.isEmpty) return
    
    while (taskQueue.nonEmpty) {
      val availableWorker = findAvailableWorker()
      
      availableWorker match {
        case Some(worker) =>
          val task = taskQueue.dequeue()
          assignTaskToWorker(task, worker)
        case None =>
          logger.debug("No available workers for task assignment")
          return
      }
    }
  }
  
  /**
   * 查找可用Worker
   */
  private def findAvailableWorker(): Option[RegisteredWorker] = {
    registeredWorkers.values
      .filter(_.status == "ACTIVE")
      .filter(worker => worker.currentTasks.size < worker.capacity)
      .toSeq
      .sortBy(_.currentTasks.size)
      .headOption
  }
  
  /**
   * 分配任务给Worker
   */
  private def assignTaskToWorker(task: PendingTask, worker: RegisteredWorker): Unit = {
    val activeTask = ActiveTask(
      taskId = task.taskId,
      taskType = task.taskType,
      assignedWorker = worker.workerId,
      assignedAt = Instant.now(),
      payload = task.payload
    )
    
    activeTasks += task.taskId -> activeTask
    worker.currentTasks += task.taskId
    
    // 发送任务给Worker
    worker.workerRef ! AssignTask(
      taskId = task.taskId,
      taskType = task.taskType,
      payload = task.payload
    )
    
    // 发布任务分配事件
    publishEvent(TaskAssigned(
      taskId = task.taskId,
      workerId = worker.workerId,
      assignedAt = Instant.now()
    ))
    
    logger.info(s"Task assigned: ${task.taskId} to worker: ${worker.workerId}")
  }
  
  /**
   * 重新分配Worker的任务
   */
  private def reassignWorkerTasks(workerId: String): Unit = {
    val workerTasks = activeTasks.filter(_._2.assignedWorker == workerId)
    
    workerTasks.foreach { case (taskId, activeTask) =>
      // 将任务重新加入队列
      val pendingTask = PendingTask(
        taskId = taskId,
        taskType = activeTask.taskType,
        priority = 5, // 默认优先级
        payload = activeTask.payload,
        submittedAt = activeTask.assignedAt
      )
      
      taskQueue.enqueue(pendingTask)
      activeTasks -= taskId
      
      logger.info(s"Reassigning task: $taskId from failed worker: $workerId")
    }
  }
  
  /**
   * 迁移状态到新Leader
   */
  private def migrateStateToNewLeader(newLeaderId: String): Unit = {
    val snapshot = MasterStateSnapshot(
      workerStates = registeredWorkers.map { case (id, worker) =>
        id -> WorkerState(
          workerId = id,
          status = worker.status,
          capacity = worker.capacity,
          currentTasks = worker.currentTasks.toList,
          lastHeartbeat = worker.lastHeartbeat
        )
      }.toMap,
      taskStates = activeTasks.map { case (id, task) =>
        id -> TaskState(
          taskId = id,
          status = "ACTIVE",
          assignedWorker = Some(task.assignedWorker),
          progress = 0.0,
          lastUpdate = task.assignedAt
        )
      }.toMap
    )
    
    clusterManager.setMasterStateSnapshot(snapshot)
    clusterManager.migrateStateTo(newLeaderId).onComplete {
      case Success(_) =>
        logger.info(s"State migration completed to: $newLeaderId")
      case Failure(ex) =>
        logger.error(s"State migration failed to: $newLeaderId", ex)
    }
  }
  
  /**
   * 订阅集群相关事件
   */
  private def subscribeToClusterEvents(): Unit = {
    subscribeToEvent(classOf[LeaderElected]) { event =>
      if (event.leaderId == nodeId) {
        logger.info(s"This node became leader for term: ${event.term}")
        // 可以在这里做一些Leader初始化工作
      } else {
        logger.info(s"New leader elected: ${event.leaderId} for term: ${event.term}")
      }
    }
    
    subscribeToEvent(classOf[LeaderStepDown]) { event =>
      if (event.leaderId == nodeId) {
        logger.info(s"This node stepped down as leader: ${event.reason}")
        // 清理Leader状态
        clearLeaderState()
      }
    }
    
    subscribeToEvent(classOf[ClusterNodeDown]) { event =>
      logger.info(s"Cluster node down: ${event.nodeId}")
      // 如果是Worker节点，清理相关状态
      if (registeredWorkers.contains(event.nodeId)) {
        reassignWorkerTasks(event.nodeId)
        registeredWorkers -= event.nodeId
      }
    }
  }
  
  /**
   * 订阅任务相关事件
   */
  private def subscribeToTaskEvents(): Unit = {
    subscribeToEvent(classOf[TaskSubmitted]) { event =>
      logger.debug(s"Task submitted event received: ${event.taskId}")
    }
    
    subscribeToEvent(classOf[TaskCompleted]) { event =>
      logger.debug(s"Task completed event received: ${event.taskId}")
    }
    
    subscribeToEvent(classOf[TaskFailed]) { event =>
      logger.debug(s"Task failed event received: ${event.taskId}")
    }
  }
  
  /**
   * 清理Leader状态
   */
  private def clearLeaderState(): Unit = {
    // 清理可以在Follower模式下不需要的状态
    // 但保留一些基本信息以便状态查询
  }
}

/**
 * HaMasterActor的伴生对象
 */
object HaMasterActor {
  def props(nodeId: String, eventBus: DataLoaderEventBus)(implicit ec: ExecutionContext): Props = {
    Props(new HaMasterActor(nodeId, eventBus))
  }
}

// ========== 消息定义 ==========

case class VoteRequest(term: Long, candidateId: String)
case class VoteResponse(term: Long, voterId: String, candidateId: String, granted: Boolean)
case class HeartbeatMessage(term: Long, leaderId: String)

case class SubmitTask(taskType: String, priority: Int, payload: Map[String, String])
case class AssignTask(taskId: String, taskType: String, payload: Map[String, String])

case object CheckHeartbeats
case object ProcessTaskQueue
case class MigrateState(newLeaderId: String)

case object GetClusterStatus
case object GetWorkerStates

// ========== 响应消息 ==========

case class HaClusterStatus(
  isLeader: Boolean,
  currentLeader: Option[String],
  term: Long,
  clusterSize: Int
)

case class WorkerStates(workers: Map[String, WorkerState])
case class NotLeaderError(currentLeader: Option[String])

// ========== 内部状态类 ==========

case class RegisteredWorker(
  workerId: String,
  workerRef: ActorRef,
  capacity: Int,
  currentTasks: mutable.Set[String],
  lastHeartbeat: Instant,
  status: String
)

case class PendingTask(
  taskId: String,
  taskType: String,
  priority: Int,
  payload: Map[String, String],
  submittedAt: Instant
)

case class ActiveTask(
  taskId: String,
  taskType: String,
  assignedWorker: String,
  assignedAt: Instant,
  payload: Map[String, String]
)
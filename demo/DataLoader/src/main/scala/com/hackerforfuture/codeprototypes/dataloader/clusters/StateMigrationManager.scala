/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.clusters

import akka.actor.{ActorRef, ActorSelection}
import akka.pattern.ask
import akka.util.Timeout
import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport
import com.hackerforfuture.codeprototypes.dataloader.events._

import java.time.Instant
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * Master状态迁移管理器
 * 
 * 负责在Master故障转移时进行状态迁移：
 * 1. 状态快照创建
 * 2. 状态序列化和传输
 * 3. 状态恢复和验证
 * 4. 迁移进度监控
 */
class StateMigrationManager(
  nodeId: String,
  eventBus: DataLoaderEventBus
)(implicit ec: ExecutionContext) extends LogSupport {

  implicit val timeout: Timeout = Timeout(30.seconds)
  
  // 迁移状态
  private var currentMigration: Option[MigrationContext] = None
  private val maxRetryAttempts = 3
  private val migrationTimeout = 60.seconds

  /**
   * 创建状态快照
   */
  def createStateSnapshot(
    masterState: MasterInternalState
  ): Future[MasterStateSnapshot] = {
    Future {
      logger.info(s"Creating state snapshot for node: $nodeId")
      
      val snapshot = MasterStateSnapshot(
        // Worker状态
        workerStates = masterState.registeredWorkers.map { case (workerId, worker) =>
          workerId -> WorkerState(
            workerId = workerId,
            status = worker.status,
            capacity = worker.capacity,
            currentTasks = worker.currentTasks.toList,
            lastHeartbeat = worker.lastHeartbeat
          )
        }.toMap,
        
        // 任务状态 
        taskStates = masterState.activeTasks.map { case (taskId, task) =>
          taskId -> TaskState(
            taskId = taskId,
            status = "ACTIVE",
            assignedWorker = Some(task.assignedWorker),
            progress = task.progress,
            lastUpdate = task.assignedAt
          )
        }.toMap,
        
        // 时间戳
        timestamp = Instant.now()
      )
      
      logger.info(s"State snapshot created: ${snapshot.estimateSize()} bytes")
      snapshot
    }
  }

  /**
   * 发起状态迁移到新Leader
   */
  def initiateStateMigration(
    snapshot: MasterStateSnapshot,
    newLeaderNodeId: String,
    newLeaderRef: ActorRef
  ): Future[MigrationResult] = {
    val migrationId = java.util.UUID.randomUUID().toString
    
    logger.info(s"Initiating state migration to new leader: $newLeaderNodeId")
    
    val migrationContext = MigrationContext(
      migrationId = migrationId,
      sourceNode = nodeId,
      targetNode = newLeaderNodeId,
      targetActor = newLeaderRef,
      snapshot = snapshot,
      startTime = Instant.now(),
      attempts = 0
    )
    
    currentMigration = Some(migrationContext)
    
    // 发布迁移开始事件
    publishMigrationEvent(MasterStateMigrationStarted(
      fromNode = nodeId,
      toNode = newLeaderNodeId,
      stateSize = snapshot.estimateSize()
    ))
    
    performStateMigration(migrationContext)
  }

  /**
   * 接收并应用状态迁移
   */
  def receiveStateMigration(
    migrationData: StateMigrationData
  ): Future[MasterInternalState] = {
    Future {
      logger.info(s"Receiving state migration from: ${migrationData.sourceNode}")
      
      val snapshot = migrationData.snapshot
      
      // 验证快照完整性
      validateSnapshot(snapshot) match {
        case Success(_) =>
          logger.info("Snapshot validation successful")
          
          // 构建内部状态
          val internalState = reconstructInternalState(snapshot)
          
          // 发布迁移完成事件
          publishMigrationEvent(MasterStateMigrationCompleted(
            fromNode = migrationData.sourceNode,
            toNode = nodeId,
            migratedItems = snapshot.workerStates.size + snapshot.taskStates.size,
            duration = java.time.Duration.between(snapshot.timestamp, Instant.now()).toMillis
          ))
          
          logger.info(s"State migration completed successfully from: ${migrationData.sourceNode}")
          internalState
          
        case Failure(ex) =>
          logger.error(s"Snapshot validation failed: ${ex.getMessage}")
          
          publishMigrationEvent(MasterStateMigrationFailed(
            fromNode = migrationData.sourceNode,
            toNode = nodeId,
            error = s"Snapshot validation failed: ${ex.getMessage}"
          ))
          
          throw ex
      }
    }
  }

  /**
   * 执行状态迁移
   */
  private def performStateMigration(context: MigrationContext): Future[MigrationResult] = {
    val migrationData = StateMigrationData(
      migrationId = context.migrationId,
      sourceNode = context.sourceNode,
      targetNode = context.targetNode,
      snapshot = context.snapshot,
      timestamp = Instant.now()
    )
    
    logger.info(s"Performing state migration attempt ${context.attempts + 1}")
    
    // 发送迁移数据到新Leader
    (context.targetActor ? migrationData).mapTo[MigrationResponse].flatMap {
      case MigrationSuccess(migrationId) =>
        logger.info(s"State migration successful: $migrationId")
        currentMigration = None
        
        publishMigrationEvent(MasterStateMigrationCompleted(
          fromNode = context.sourceNode,
          toNode = context.targetNode,
          migratedItems = context.snapshot.workerStates.size + context.snapshot.taskStates.size,
          duration = java.time.Duration.between(context.startTime, Instant.now()).toMillis
        ))
        
        Future.successful(MigrationResult.Success(migrationId))
        
      case MigrationFailure(migrationId, error) =>
        logger.warn(s"State migration failed: $error")
        
        // 重试逻辑
        if (context.attempts < maxRetryAttempts) {
          val retryContext = context.copy(attempts = context.attempts + 1)
          currentMigration = Some(retryContext)
          
          logger.info(s"Retrying state migration, attempt ${retryContext.attempts}")
          
          // 延迟重试
          Future {
            Thread.sleep(5000) // 等待5秒后重试
          }(ec).flatMap { _ =>
            performStateMigration(retryContext)
          }
        } else {
          logger.error(s"State migration failed after ${maxRetryAttempts} attempts")
          currentMigration = None
          
          publishMigrationEvent(MasterStateMigrationFailed(
            fromNode = context.sourceNode,
            toNode = context.targetNode,
            error = s"Migration failed after $maxRetryAttempts attempts: $error"
          ))
          
          Future.successful(MigrationResult.Failure(error))
        }
        
    }.recoverWith {
      case ex =>
        logger.error(s"State migration communication error: ${ex.getMessage}", ex)
        
        if (context.attempts < maxRetryAttempts) {
          val retryContext = context.copy(attempts = context.attempts + 1)
          // 递归调用进行重试
          performStateMigration(retryContext)
        } else {
          publishMigrationEvent(MasterStateMigrationFailed(
            fromNode = context.sourceNode,
            toNode = context.targetNode,
            error = s"Communication error: ${ex.getMessage}"
          ))
          
          Future.successful(MigrationResult.Failure(ex.getMessage))
        }
    }
  }

  /**
   * 验证快照完整性
   */
  private def validateSnapshot(snapshot: MasterStateSnapshot): scala.util.Try[Unit] = {
    scala.util.Try {
      // 基本验证
      require(snapshot.timestamp != null, "Timestamp cannot be null")
      
      // 验证Worker状态
      snapshot.workerStates.foreach { case (workerId, worker) =>
        require(workerId == worker.workerId, s"Worker ID mismatch: $workerId != ${worker.workerId}")
        require(worker.capacity > 0, s"Worker capacity must be positive: ${worker.capacity}")
        require(worker.currentTasks.forall(_.nonEmpty), s"Invalid task IDs in worker: $workerId")
      }
      
      // 验证任务状态
      snapshot.taskStates.foreach { case (taskId, task) =>
        require(taskId == task.taskId, s"Task ID mismatch: $taskId != ${task.taskId}")
        require(task.progress >= 0.0 && task.progress <= 1.0, s"Invalid progress: ${task.progress}")
      }
      
      // 验证任务分配一致性
      val assignedTasks = snapshot.taskStates.values
        .filter(_.assignedWorker.isDefined)
        .map(task => task.taskId -> task.assignedWorker.get)
        .toMap
      
      assignedTasks.foreach { case (taskId, workerId) =>
        snapshot.workerStates.get(workerId) match {
          case Some(worker) =>
            require(worker.currentTasks.contains(taskId), 
              s"Task $taskId assigned to worker $workerId but not in worker's task list")
          case None =>
            throw new IllegalStateException(s"Task $taskId assigned to non-existent worker: $workerId")
        }
      }
      
      logger.debug("Snapshot validation completed successfully")
    }
  }

  /**
   * 从快照重构内部状态
   */
  private def reconstructInternalState(snapshot: MasterStateSnapshot): MasterInternalState = {
    logger.info("Reconstructing internal state from snapshot")
    
    // 重构Worker状态
    val registeredWorkers = snapshot.workerStates.map { case (workerId, workerState) =>
      workerId -> RegisteredWorkerInternal(
        workerId = workerId,
        workerRef = null, // 需要重新建立连接
        capacity = workerState.capacity,
        currentTasks = scala.collection.mutable.Set(workerState.currentTasks: _*),
        lastHeartbeat = workerState.lastHeartbeat,
        status = workerState.status,
        metrics = None // WorkerState没有metrics字段
      )
    }
    
    // 重构活动任务
    val activeTasks = snapshot.taskStates.map { case (taskId, taskState) =>
      taskId -> ActiveTaskInternal(
        taskId = taskId,
        taskType = "default", // TaskState没有taskType字段，使用默认值
        assignedWorker = taskState.assignedWorker.getOrElse(""),
        assignedAt = taskState.lastUpdate,
        payload = Map.empty, // TaskState没有payload字段，使用空Map
        progress = taskState.progress
      )
    }
    
    // 重构待处理任务队列（暂时为空，因为快照中没有pendingTasks）
    val taskQueue = scala.collection.mutable.Queue[PendingTaskInternal]()
    
    val internalState = MasterInternalState(
      currentTerm = 1L, // 默认值，实际应该从其他地方获取
      registeredWorkers = scala.collection.mutable.Map(registeredWorkers.toSeq: _*),
      activeTasks = scala.collection.mutable.Map(activeTasks.toSeq: _*),
      taskQueue = taskQueue,
      clusterSize = registeredWorkers.size, // 基于Worker数量估算
      activeNodes = scala.collection.mutable.Set(registeredWorkers.keys.toSeq: _*)
    )
    
    logger.info(s"Internal state reconstructed: " +
      s"${internalState.registeredWorkers.size} workers, " +
      s"${internalState.activeTasks.size} active tasks, " +
      s"${internalState.taskQueue.size} pending tasks")
    
    internalState
  }

  /**
   * 获取当前迁移状态
   */
  def getCurrentMigrationStatus(): Option[MigrationStatus] = {
    currentMigration.map { context =>
      MigrationStatus(
        migrationId = context.migrationId,
        sourceNode = context.sourceNode,
        targetNode = context.targetNode,
        progress = calculateMigrationProgress(context),
        status = "IN_PROGRESS",
        attempts = context.attempts,
        startTime = context.startTime
      )
    }
  }

  /**
   * 取消当前迁移
   */
  def cancelMigration(reason: String): Unit = {
    currentMigration.foreach { context =>
      logger.warn(s"Cancelling migration ${context.migrationId}: $reason")
      
      publishMigrationEvent(MasterStateMigrationFailed(
        fromNode = context.sourceNode,
        toNode = context.targetNode,
        error = s"Migration cancelled: $reason"
      ))
      
      currentMigration = None
    }
  }

  /**
   * 计算迁移进度
   */
  private def calculateMigrationProgress(context: MigrationContext): Double = {
    val elapsed = java.time.Duration.between(context.startTime, Instant.now()).toMillis
    val timeoutMillis = migrationTimeout.toMillis
    
    // 基于时间的简单进度估算
    math.min(elapsed.toDouble / timeoutMillis, 0.95)
  }

  /**
   * 发布迁移事件
   */
  private def publishMigrationEvent(event: DomainEvent): Unit = {
    eventBus.publishAsync(event).foreach { _ =>
      logger.debug(s"Published migration event: ${event.eventType}")
    }
  }
}

// ========== 状态迁移相关类 ==========

/**
 * 迁移上下文
 */
case class MigrationContext(
  migrationId: String,
  sourceNode: String,
  targetNode: String,
  targetActor: ActorRef,
  snapshot: MasterStateSnapshot,
  startTime: Instant,
  attempts: Int
)

/**
 * 状态迁移数据
 */
case class StateMigrationData(
  migrationId: String,
  sourceNode: String,
  targetNode: String,
  snapshot: MasterStateSnapshot,
  timestamp: Instant
)

/**
 * 迁移响应
 */
sealed trait MigrationResponse
case class MigrationSuccess(migrationId: String) extends MigrationResponse
case class MigrationFailure(migrationId: String, error: String) extends MigrationResponse

/**
 * 迁移结果
 */
sealed trait MigrationResult
object MigrationResult {
  case class Success(migrationId: String) extends MigrationResult
  case class Failure(error: String) extends MigrationResult
}

/**
 * 迁移状态信息
 */
case class MigrationStatus(
  migrationId: String,
  sourceNode: String,
  targetNode: String,
  progress: Double,
  status: String,
  attempts: Int,
  startTime: Instant
)

/**
 * 扩展的Master状态快照（使用ClusterManager中定义的MasterStateSnapshot）
 */
// 使用 import 来引用 ClusterManager 中定义的 MasterStateSnapshot

/**
 * 待处理任务状态
 */
case class PendingTaskState(
  taskId: String,
  taskType: String,
  priority: Int,
  payload: Map[String, String],
  submittedAt: Instant
)

/**
 * 集群元数据
 */
case class ClusterMetadata(
  clusterSize: Int,
  activeNodes: List[String],
  lastElectionTerm: Long
)

/**
 * Master内部状态（用于状态重构）
 */
case class MasterInternalState(
  currentTerm: Long,
  registeredWorkers: scala.collection.mutable.Map[String, RegisteredWorkerInternal],
  activeTasks: scala.collection.mutable.Map[String, ActiveTaskInternal],
  taskQueue: scala.collection.mutable.Queue[PendingTaskInternal],
  clusterSize: Int,
  activeNodes: scala.collection.mutable.Set[String]
)

/**
 * 内部Worker状态
 */
case class RegisteredWorkerInternal(
  workerId: String,
  workerRef: ActorRef,
  capacity: Int,
  currentTasks: scala.collection.mutable.Set[String],
  lastHeartbeat: Instant,
  status: String,
  metrics: Option[Map[String, String]] = None
)

/**
 * 内部活动任务
 */
case class ActiveTaskInternal(
  taskId: String,
  taskType: String,
  assignedWorker: String,
  assignedAt: Instant,
  payload: Map[String, String],
  progress: Double = 0.0
)

/**
 * 内部待处理任务
 */
case class PendingTaskInternal(
  taskId: String,
  taskType: String,
  priority: Int,
  payload: Map[String, String],
  submittedAt: Instant
)
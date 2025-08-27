/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.clusters.master

import akka.actor.{ActorPath, Props, Terminated}
import com.hackerforfuture.codeprototypes.dataloader.clusters._
import com.hackerforfuture.codeprototypes.dataloader.common.Using
import com.hackerforfuture.codeprototypes.dataloader.events._

import java.time.Instant
import scala.collection.mutable
import scala.concurrent.duration._

/**
 * 事件驱动的Master Actor
 *
 * 功能增强：
 * - 发布集群管理相关事件
 * - 订阅任务管理事件
 * - 支持事件溯源和状态恢复
 * - 更智能的故障检测和恢复
 */
class EventDrivenMasterActor(override val eventBus: DataLoaderEventBus)
  extends HybridMessageEventActor with EventHandler with Using {

  // 存储已注册的SlaveActor的唯一标识ID和最后一次收到心跳的时间戳
  private final val registeredSlaves = mutable.HashMap.empty[ActorPath, WorkerInfo]

  // 心跳超时时间
  private final val heartbeatTimeout: FiniteDuration = 10.seconds
  private final val initialTimeout: FiniteDuration = 5.seconds

  /**
   * Worker信息数据结构
   */
  case class WorkerInfo(
    path: ActorPath,
    lastHeartbeat: Long,
    registeredAt: Instant,
    capabilities: Set[String] = Set.empty,
    taskCount: Int = 0
  )

  override def preStart(): Unit = {
    super.preStart()

    // 启动心跳检查定时器
    import context.dispatcher
    context.system.scheduler.schedule(initialTimeout, heartbeatTimeout, self, CheckHeartbeat)

    // 发布Master选举事件
    publishEvent(MasterElected(
      masterId = actorId,
      masterPath = self.path
    ))
  }

  /**
   * 设置事件订阅
   */
  override protected def setupEventSubscriptions(): Unit = {
    // 订阅任务相关事件
    subscribeToEvent(classOf[TaskSubmitted]) { event =>
      handleTaskSubmittedEvent(event)
    }

    subscribeToEvent(classOf[TaskCompleted]) { event =>
      handleTaskCompletedEvent(event)
    }

    subscribeToEvent(classOf[TaskFailed]) { event =>
      handleTaskFailedEvent(event)
    }
  }

  /**
   * 消息到事件的转换
   */
  override protected def messageToEvent: PartialFunction[Message, DomainEvent] = {
    case Heartbeat =>
      val senderId = sender().path
      HeartbeatReceived(
        workerId = senderId.name,
        workerPath = senderId
      )

    case register: Register =>
      val senderId = sender().path
      WorkerRegistered(
        workerId = register.workerId,
        workerPath = senderId
      )

    case SlaveActorTerminated(id, reason) =>
      WorkerUnregistered(
        workerId = id.name,
        workerPath = id,
        reason = reason
      )
  }

  /**
   * 处理传统消息
   */
  override protected def handleMessage(message: Message): Unit = {
    message match {
      case Heartbeat => handleHeartbeatEvent()
      case register: Register => handleRegisterEvent()
      case CheckHeartbeat => handelCheckHeartbeatEvent()
      case StopActor => handleStopEvent()
      case msg: CustomMessage => handleCustomMessage(msg.id.getOrElse(sender().path), msg.content)
      case SlaveActorTerminated(id, reason) => handleSlaveTerminated(id, reason)
      case _ => log.warning(s"Unknown message: $message")
    }
  }

  /**
   * 处理心跳事件
   */
  override def handleHeartbeatEvent(): Unit = {
    syncableBlock {
      val senderId: ActorPath = sender().path
      val ts: Long = System.currentTimeMillis()

      if (registeredSlaves.contains(senderId)) {
        // 更新现有worker信息
        val workerInfo = registeredSlaves(senderId)
        registeredSlaves.put(senderId, workerInfo.copy(lastHeartbeat = ts))
        log.debug(s"[${ts}] Updated heartbeat from worker: ${senderId.name}")
      } else {
        // 注册新worker
        val workerInfo = WorkerInfo(
          path = senderId,
          lastHeartbeat = ts,
          registeredAt = Instant.now()
        )
        registeredSlaves.put(senderId, workerInfo)
        context.watch(sender())
        log.info(s"[${ts}] Registered new worker: ${senderId.name}")
      }
    }
  }

  /**
   * 处理注册事件
   */
  override def handleRegisterEvent(): Unit = handleHeartbeatEvent()

  /**
   * 处理心跳检查事件
   */
  override def handelCheckHeartbeatEvent(): Unit = {
    val currentTime: Long = System.currentTimeMillis()
    val timedOutWorkers = registeredSlaves.filter {
      case (_, workerInfo) =>
        val elapsed = currentTime - workerInfo.lastHeartbeat
        elapsed > (heartbeatTimeout.toMillis * 2) // 2倍超时时间
    }

    timedOutWorkers.foreach { case (workerPath, workerInfo) =>
      registeredSlaves -= workerPath
      log.warning(s"Worker ${workerPath.name} timed out and unregistered.")

      // 发布超时事件
      publishEvent(WorkerTimedOut(
        workerId = workerPath.name,
        workerPath = workerPath,
        lastHeartbeat = Instant.ofEpochMilli(workerInfo.lastHeartbeat)
      ))

      self ! SlaveActorTerminated(workerPath, "Heartbeat timeout")
    }
  }

  /**
   * 处理停止事件
   */
  override def handleStopEvent(): Unit = {
    log.info("Received StopActor message, shutting down...")

    // 通知所有worker停止
    registeredSlaves.foreach { case (workerPath, _) =>
      log.info(s"Stopping worker: ${workerPath.name}")
      context.system.actorSelection(workerPath) ! StopActor
    }

    registeredSlaves.clear()
    context.stop(self)
  }

  /**
   * 处理自定义消息
   */
  private def handleCustomMessage(id: ActorPath, content: Any): Unit = {
    if (registeredSlaves.contains(id)) {
      log.info(s"Processing message from worker ${id.name}: $content")
      // TODO: 处理具体的业务逻辑
    } else {
      log.warning(s"Received message from unregistered worker: ${id.name}")
      context.system.actorSelection(id) ! RegisterTimeout
    }
  }

  /**
   * 处理worker终止
   */
  private def handleSlaveTerminated(id: ActorPath, reason: String): Unit = {
    registeredSlaves.remove(id)
    log.warning(s"Worker ${id.name} terminated. Reason: $reason")
  }

  /**
   * 处理Actor终止消息
   */
  private def handleTerminated(actor: akka.actor.ActorRef): Unit = {
    val actorId = actor.path
    registeredSlaves.remove(actorId)
    log.warning(s"Worker ${actorId.name} terminated.")

    publishEvent(WorkerUnregistered(
      workerId = actorId.name,
      workerPath = actorId,
      reason = "Actor terminated"
    ))
  }

  /**
   * 完整的消息处理器
   */
  override def receive: Receive = {
    case Terminated(actor) => handleTerminated(actor)
    case msg: Message =>
      handleMessage(msg)
      messageToEvent.lift(msg).foreach(publishEventAsync)
    case other =>
      log.warning(s"Unhandled message: $other")
  }

  // ========== 事件处理器 ==========

  /**
   * 处理任务提交事件
   */
  private def handleTaskSubmittedEvent(event: TaskSubmitted): Unit = {
    log.info(s"New task submitted: ${event.taskId} with priority ${event.priority}")

    // 选择合适的worker分配任务
    selectWorkerForTask(event.taskId, event.taskType) match {
      case Some(workerPath) =>
        // 发布任务分配事件
        publishEvent(TaskAssigned(
          taskId = event.taskId,
          workerId = workerPath.name,
          correlationId = event.correlationId
        ))

        // 更新worker任务计数
        registeredSlaves.get(workerPath).foreach { workerInfo =>
          registeredSlaves.put(workerPath, workerInfo.copy(taskCount = workerInfo.taskCount + 1))
        }

        log.info(s"Task ${event.taskId} assigned to worker ${workerPath.name}")

      case None =>
        log.warning(s"No available worker for task ${event.taskId}")
      // TODO: 将任务放入待分配队列
    }
  }

  /**
   * 处理任务完成事件
   */
  private def handleTaskCompletedEvent(event: TaskCompleted): Unit = {
    log.info(s"Task ${event.taskId} completed by worker ${event.workerId}")

    // 更新worker任务计数
    registeredSlaves.find(_._1.name == event.workerId).foreach { case (workerPath, workerInfo) =>
      registeredSlaves.put(workerPath, workerInfo.copy(taskCount = math.max(0, workerInfo.taskCount - 1)))
    }
  }

  /**
   * 处理任务失败事件
   */
  private def handleTaskFailedEvent(event: TaskFailed): Unit = {
    log.warning(s"Task ${event.taskId} failed on worker ${event.workerId}: ${event.error}")

    // 更新worker任务计数
    registeredSlaves.find(_._1.name == event.workerId).foreach { case (workerPath, workerInfo) =>
      registeredSlaves.put(workerPath, workerInfo.copy(taskCount = math.max(0, workerInfo.taskCount - 1)))
    }

    // TODO: 实现任务重试逻辑
    if (event.retryCount < 3) {
      log.info(s"Retrying task ${event.taskId}, attempt ${event.retryCount + 1}")
      // 重新提交任务
    }
  }

  /**
   * 为任务选择合适的worker
   */
  private def selectWorkerForTask(taskId: String, taskType: String): Option[ActorPath] = {
    if (registeredSlaves.isEmpty) {
      None
    } else {
      // 简单的负载均衡：选择任务数最少的worker
      registeredSlaves.find(_._2.taskCount == registeredSlaves.values.map(_.taskCount).min).map(_._1)
    }
  }

  /**
   * 获取集群状态
   */
  def getClusterStatus: ClusterStatus = {
    ClusterStatus(
      masterId = actorId,
      totalWorkers = registeredSlaves.size,
      activeWorkers = registeredSlaves.count(_._2.lastHeartbeat > System.currentTimeMillis() - heartbeatTimeout.toMillis),
      totalTasks = registeredSlaves.values.map(_.taskCount).sum
    )
  }

  override def handleRegisterTimeout(): Unit = {
    // 处理注册超时
  }
}

/**
 * 集群状态信息
 */
case class ClusterStatus(
  masterId: String,
  totalWorkers: Int,
  activeWorkers: Int,
  totalTasks: Int
)

/**
 * 伴生对象
 */
object EventDrivenMasterActor {
  def props(eventBus: DataLoaderEventBus): Props = Props(new EventDrivenMasterActor(eventBus))
}


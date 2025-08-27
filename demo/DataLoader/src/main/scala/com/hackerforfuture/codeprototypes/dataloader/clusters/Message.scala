package com.hackerforfuture.codeprototypes.dataloader.clusters

import akka.actor.ActorPath
import java.time.Instant

/**
 * Author: biyu.huang
 * Date: 2023/11/1 19:06
 * Description: 集群消息定义
 */
sealed trait Message

// 基础控制消息
case object Heartbeat extends Message
case object CheckHeartbeat extends Message 
case object CheckHeartbeats extends Message  // HaMasterActor使用的版本
case object RegisterTimeout extends Message
case object StopActor extends Message

// Worker注册相关消息
case class Register(
  workerId: String,
  capacity: Option[Int] = None,
  metadata: Map[String, Any] = Map.empty
) extends Message

case class RegisteredConfirmation(workerId: String) extends Message

case class WorkerNotRegistered(workerId: String) extends Message

// 心跳相关消息
case class HeartBeat(
  workerId: String,
  status: Option[String] = None,
  metrics: Option[Map[String, Any]] = None,
  timestamp: Instant = Instant.now()
) extends Message

case class HeartBeatAck(workerId: String) extends Message

// 任务处理消息  
case object ProcessTaskQueue extends Message
case object SendHeartbeat extends Message
case object ReportTaskStatus extends Message

// Master发现消息
case object DiscoverMaster extends Message

// 任务分配消息
case class AssignTask(
  taskId: String,
  taskType: String,
  payload: Map[String, Any] = Map.empty
) extends Message

case class TaskExecution(
  taskId: String,
  taskType: String,
  payload: Map[String, Any] = Map.empty
) extends Message

// 自定义消息类型，带有完整字段
case class CustomMessage(
  messageType: String,
  data: Map[String, String] = Map.empty,
  id: Option[ActorPath] = None,
  content: Option[Any] = None
) extends Message

// Actor生命周期消息
case class SlaveActorTerminated(id: ActorPath, reason: String) extends Message

// 任务提交消息
case class SubmitTask(
  taskId: String,
  taskType: String,
  priority: Int = 0,
  payload: Map[String, Any] = Map.empty
) extends Message

// 集群状态查询
case object GetClusterStatus extends Message
case object GetWorkerStatus extends Message

// 状态迁移消息
case class MigrateState(targetNode: String) extends Message

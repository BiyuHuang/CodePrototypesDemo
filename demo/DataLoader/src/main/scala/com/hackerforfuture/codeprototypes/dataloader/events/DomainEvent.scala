/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.events

import akka.actor.ActorPath

import java.time.Instant
import java.util.UUID

/**
 * 领域事件基类 - 系统中所有事件的抽象基类
 * 
 * 设计原则：
 * - 事件是不可变的，表示已经发生的事实
 * - 包含必要的元数据用于事件溯源和调试
 * - 支持事件版本控制以便向后兼容
 */
sealed trait DomainEvent {
  def eventId: String
  def eventType: String
  def timestamp: Instant
  def aggregateId: String
  def version: Int
  def correlationId: Option[String]
}

/**
 * 抽象事件基类，提供通用字段的默认实现
 */
abstract class AbstractDomainEvent(
  val eventId: String = UUID.randomUUID().toString,
  val timestamp: Instant = Instant.now(),
  val aggregateId: String,
  val version: Int = 1,
  val correlationId: Option[String] = None
) extends DomainEvent {
  val eventType: String = this.getClass.getSimpleName
}

// ========== 集群管理相关事件 ==========

/**
 * 工作节点注册事件
 */
case class WorkerRegistered(
  workerId: String,
  workerPath: ActorPath,
  capabilities: Set[String] = Set.empty,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = workerId)

/**
 * 工作节点注销事件
 */
case class WorkerUnregistered(
  workerId: String,
  workerPath: ActorPath,
  reason: String,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = workerId)

/**
 * 心跳接收事件
 */
case class HeartbeatReceived(
  workerId: String,
  workerPath: ActorPath,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = workerId)

/**
 * 工作节点超时事件
 */
case class WorkerTimedOut(
  workerId: String,
  workerPath: ActorPath,
  lastHeartbeat: Instant,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = workerId)

/**
 * 工作节点断连事件
 */
case class WorkerDisconnected(
  workerId: String,
  reason: String,
  disconnectedAt: Instant = Instant.now(),
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = workerId)

/**
 * 工作节点重连事件
 */
case class WorkerReconnected(
  workerId: String,
  reconnectedAt: Instant = Instant.now(),
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = workerId)

/**
 * 集群节点状态事件
 */
case class ClusterNodeUp(
  nodeId: String,
  nodeAddress: String,
  nodeRoles: Set[String],
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = nodeId)

case class ClusterNodeDown(
  nodeId: String,
  nodeAddress: String,
  reason: String,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = nodeId)

case class ClusterNodeUnreachable(
  nodeId: String,
  nodeAddress: String,
  observedBy: String,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = nodeId)

case class ClusterNodeReachable(
  nodeId: String,
  nodeAddress: String,
  observedBy: String,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = nodeId)

/**
 * Leader选举相关事件
 */
case class LeaderElectionStarted(
  term: Long,
  candidateId: String,
  clusterSize: Int,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = candidateId)

case class VoteCasted(
  term: Long,
  voterId: String,
  candidateId: String,
  voteGranted: Boolean,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = candidateId)

case class LeaderElected(
  term: Long,
  leaderId: String,
  followerIds: List[String],
  electionDuration: Long,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = leaderId)

case class LeaderHeartbeat(
  term: Long,
  leaderId: String,
  followerCount: Int,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = leaderId)

case class LeaderStepDown(
  term: Long,
  leaderId: String,
  reason: String,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = leaderId)

/**
 * 集群角色变更事件
 */
case class NodeBecameLeader(
  nodeId: String,
  term: Long,
  previousLeader: Option[String],
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = nodeId)

case class NodeBecameFollower(
  nodeId: String,
  term: Long,
  newLeader: String,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = nodeId)

case class NodeBecameCandidate(
  nodeId: String,
  term: Long,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = nodeId)

/**
 * 集群状态迁移事件
 */
case class MasterStateMigrationStarted(
  fromNode: String,
  toNode: String,
  stateSize: Long,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = toNode)

case class MasterStateMigrationCompleted(
  fromNode: String,
  toNode: String,
  migratedItems: Int,
  duration: Long,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = toNode)

case class MasterStateMigrationFailed(
  fromNode: String,
  toNode: String,
  error: String,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = toNode)

/**
 * 保持向后兼容的Master选举事件
 */
@deprecated("Use LeaderElected instead", "1.1.0")
case class MasterElected(
  masterId: String,
  masterPath: ActorPath,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = masterId)

// ========== 任务管理相关事件 ==========

/**
 * 任务提交事件
 */
case class TaskSubmitted(
  taskId: String,
  taskType: String,
  priority: Int,
  submittedBy: String,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = taskId)

/**
 * 任务分配事件
 */
case class TaskAssigned(
  taskId: String,
  workerId: String,
  assignedAt: Instant = Instant.now(),
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = taskId)

/**
 * 任务开始执行事件
 */
case class TaskStarted(
  taskId: String,
  workerId: String,
  startedAt: Instant = Instant.now(),
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = taskId)

/**
 * 任务完成事件
 */
case class TaskCompleted(
  taskId: String,
  workerId: String,
  result: TaskResult,
  completedAt: Instant = Instant.now(),
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = taskId)

/**
 * 任务失败事件
 */
case class TaskFailed(
  taskId: String,
  workerId: String,
  error: String,
  failedAt: Instant = Instant.now(),
  retryCount: Int = 0,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = taskId)

/**
 * 任务取消事件
 */
case class TaskCancelled(
  taskId: String,
  reason: String,
  cancelledBy: String,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = taskId)

// ========== 数据处理相关事件 ==========

/**
 * 数据扫描开始事件
 */
case class DataScanStarted(
  scanId: String,
  scanType: String,
  targetPath: String,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = scanId)

/**
 * 数据发现事件
 */
case class DataDiscovered(
  scanId: String,
  fileCount: Int,
  totalSize: Long,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = scanId)

/**
 * 数据上传开始事件
 */
case class DataUploadStarted(
  uploadId: String,
  sourceFile: String,
  targetLocation: String,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = uploadId)

/**
 * 数据上传完成事件
 */
case class DataUploadCompleted(
  uploadId: String,
  sourceFile: String,
  targetLocation: String,
  bytesTransferred: Long,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = uploadId)

/**
 * 数据下载开始事件
 */
case class DataDownloadStarted(
  downloadId: String,
  sourceLocation: String,
  targetFile: String,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = downloadId)

/**
 * 数据下载完成事件
 */
case class DataDownloadCompleted(
  downloadId: String,
  sourceLocation: String,
  targetFile: String,
  bytesTransferred: Long,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = downloadId)

// ========== 系统管理相关事件 ==========

/**
 * 系统启动事件
 */
case class SystemStarted(
  systemId: String,
  systemVersion: String,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = systemId)

/**
 * 系统关闭事件
 */
case class SystemShutdown(
  systemId: String,
  reason: String,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = systemId)

/**
 * 配置更新事件
 */
case class ConfigurationUpdated(
  configKey: String,
  oldValue: Option[String],
  newValue: String,
  updatedBy: String,
  override val correlationId: Option[String] = None
) extends AbstractDomainEvent(aggregateId = configKey)

// ========== 支持类型定义 ==========

/**
 * 任务执行结果
 */
sealed trait TaskResult

case class SuccessResult(
  message: String,
  data: Option[Any] = None
) extends TaskResult

case class FailureResult(
  error: String,
  cause: Option[Throwable] = None
) extends TaskResult

case class PartialResult(
  completedItems: Int,
  totalItems: Int,
  errors: List[String] = List.empty
) extends TaskResult


/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.clusters

import akka.actor.{ActorRef, Address}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport
import com.hackerforfuture.codeprototypes.dataloader.events._

import java.time.Instant
import java.util.concurrent.atomic.{AtomicLong, AtomicReference}
import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/**
 * 集群状态枚举
 */
sealed trait ClusterState
case object Initializing extends ClusterState
case object Follower extends ClusterState
case object Candidate extends ClusterState
case object Leader extends ClusterState
case object Shutdown extends ClusterState

/**
 * 选举状态
 */
case class ElectionState(
  currentTerm: Long,
  votedFor: Option[String],
  votes: mutable.Set[String] = mutable.Set.empty,
  lastHeartbeat: Instant = Instant.now()
)

/**
 * 集群节点信息
 */
case class ClusterNode(
  nodeId: String,
  address: Address,
  roles: Set[String],
  isReachable: Boolean = true,
  lastSeen: Instant = Instant.now()
)

/**
 * 集群管理器 - 负责Leader选举和集群状态管理
 * 
 * 实现简化版的Raft共识算法：
 * 1. Leader选举
 * 2. 心跳机制
 * 3. 故障检测
 * 4. 状态迁移
 */
class ClusterManager(
  nodeId: String,
  cluster: Cluster,
  eventBus: DataLoaderEventBus
)(implicit ec: ExecutionContext) extends LogSupport {

  // 集群状态
  private val currentState = new AtomicReference[ClusterState](Initializing)
  private val electionState = new AtomicReference(ElectionState(0L, None))
  private val currentTerm = new AtomicLong(0L)
  
  // 集群成员信息
  private val clusterNodes = mutable.Map[String, ClusterNode]()
  private val masterNodes = mutable.Set[String]()
  private var currentLeader: Option[String] = None
  
  // 选举相关
  private val electionTimeoutMin = 5.seconds
  private val electionTimeoutMax = 10.seconds
  private val heartbeatInterval = 2.seconds
  
  // 状态同步
  private var masterState: Option[MasterStateSnapshot] = None
  
  /**
   * 启动集群管理器
   */
  def start(): Unit = {
    logger.info(s"Starting ClusterManager for node: $nodeId")
    
    // 订阅集群事件 (Akka 2.5 API)
    cluster.subscribe(
      ActorRef.noSender,
      InitialStateAsEvents,
      classOf[MemberEvent],
      classOf[UnreachableMember],
      classOf[ReachableMember],
      classOf[LeaderChanged]
    )
    
    // 注册当前节点
    if (cluster.selfRoles.contains("master")) {
      masterNodes += nodeId
      scheduleElectionTimeout()
    }
    
    currentState.set(Follower)
    publishClusterEvent(ClusterNodeUp(
      nodeId = nodeId,
      nodeAddress = cluster.selfAddress.toString,
      nodeRoles = cluster.selfRoles
    ))
    
    logger.info(s"ClusterManager started for node: $nodeId with roles: ${cluster.selfRoles}")
  }
  
  /**
   * 停止集群管理器
   */
  def stop(): Unit = {
    logger.info(s"Stopping ClusterManager for node: $nodeId")
    currentState.set(Shutdown)
    cluster.unsubscribe(ActorRef.noSender)
    
    if (isLeader) {
      stepDown("Node shutdown")
    }
  }
  
  /**
   * 处理集群事件
   */
  def handleClusterEvent(event: akka.cluster.ClusterEvent.ClusterDomainEvent): Unit = {
    event match {
      case MemberUp(member) =>
        handleMemberUp(member)
        
      case MemberRemoved(member, previousStatus) =>
        handleMemberRemoved(member, previousStatus.toString)
        
      case UnreachableMember(member) =>
        handleMemberUnreachable(member)
        
      case ReachableMember(member) =>
        handleMemberReachable(member)
        
      case LeaderChanged(leader) =>
        handleLeaderChanged(leader)
        
      case _ =>
        logger.debug(s"Unhandled cluster event: $event")
    }
  }
  
  /**
   * 开始选举
   */
  def startElection(): Future[Boolean] = {
    if (currentState.get() != Follower && currentState.get() != Candidate) {
      return Future.successful(false)
    }
    
    val newTerm = currentTerm.incrementAndGet()
    currentState.set(Candidate)
    
    val state = electionState.get()
    val newState = state.copy(
      currentTerm = newTerm,
      votedFor = Some(nodeId),
      votes = mutable.Set(nodeId)
    )
    electionState.set(newState)
    
    logger.info(s"Starting election for term $newTerm")
    publishClusterEvent(LeaderElectionStarted(
      term = newTerm,
      candidateId = nodeId,
      clusterSize = masterNodes.size
    ))
    
    // 为自己投票
    publishClusterEvent(VoteCasted(
      term = newTerm,
      voterId = nodeId,
      candidateId = nodeId,
      voteGranted = true
    ))
    
    // 请求其他节点投票
    requestVotes(newTerm)
    
    // 检查是否获得多数票
    checkElectionResult(newTerm)
  }
  
  /**
   * 处理投票请求
   */
  def handleVoteRequest(term: Long, candidateId: String): Boolean = {
    val state = electionState.get()
    val currentTermValue = currentTerm.get()
    
    // 如果收到更高term的请求，更新term并转为Follower
    if (term > currentTermValue) {
      currentTerm.set(term)
      currentState.set(Follower)
      currentLeader = None
    }
    
    // 投票条件：term有效 且 未投票或已投给该候选人
    val canVote = term >= currentTermValue && 
                  (state.votedFor.isEmpty || state.votedFor.contains(candidateId))
    
    if (canVote) {
      electionState.set(state.copy(votedFor = Some(candidateId)))
      logger.info(s"Voted for $candidateId in term $term")
      
      publishClusterEvent(VoteCasted(
        term = term,
        voterId = nodeId,
        candidateId = candidateId,
        voteGranted = true
      ))
      true
    } else {
      logger.info(s"Rejected vote for $candidateId in term $term")
      publishClusterEvent(VoteCasted(
        term = term,
        voterId = nodeId,
        candidateId = candidateId,
        voteGranted = false
      ))
      false
    }
  }
  
  /**
   * 处理投票结果
   */
  def handleVoteResult(term: Long, voterId: String, candidateId: String, granted: Boolean): Unit = {
    if (candidateId == nodeId && term == currentTerm.get() && currentState.get() == Candidate) {
      val state = electionState.get()
      
      if (granted) {
        state.votes += voterId
        logger.debug(s"Received vote from $voterId, total votes: ${state.votes.size}")
        
        // 检查是否获得多数票
        if (state.votes.size > masterNodes.size / 2) {
          becomeLeader(term)
        }
      }
    }
  }
  
  /**
   * 成为Leader
   */
  private def becomeLeader(term: Long): Unit = {
    logger.info(s"Became leader for term $term")
    currentState.set(Leader)
    currentLeader = Some(nodeId)
    
    val followers = masterNodes.filterNot(_ == nodeId).toList
    publishClusterEvent(LeaderElected(
      term = term,
      leaderId = nodeId,
      followerIds = followers,
      electionDuration = 0L // TODO: 计算选举持续时间
    ))
    
    publishClusterEvent(NodeBecameLeader(
      nodeId = nodeId,
      term = term,
      previousLeader = None // TODO: 跟踪前任Leader
    ))
    
    // 开始发送心跳
    scheduleHeartbeat()
  }
  
  /**
   * 退位
   */
  def stepDown(reason: String): Unit = {
    if (isLeader) {
      logger.info(s"Stepping down as leader: $reason")
      
      publishClusterEvent(LeaderStepDown(
        term = currentTerm.get(),
        leaderId = nodeId,
        reason = reason
      ))
      
      currentState.set(Follower)
      currentLeader = None
      scheduleElectionTimeout()
    }
  }
  
  /**
   * 发送心跳
   */
  private def sendHeartbeat(): Unit = {
    if (isLeader) {
      val term = currentTerm.get()
      val followerCount = masterNodes.size - 1
      
      publishClusterEvent(LeaderHeartbeat(
        term = term,
        leaderId = nodeId,
        followerCount = followerCount
      ))
      
      // 更新心跳时间
      val state = electionState.get()
      electionState.set(state.copy(lastHeartbeat = Instant.now()))
      
      // 调度下次心跳
      scheduleHeartbeat()
    }
  }
  
  /**
   * 处理心跳
   */
  def handleHeartbeat(term: Long, leaderId: String): Unit = {
    val currentTermValue = currentTerm.get()
    
    if (term >= currentTermValue) {
      if (term > currentTermValue) {
        currentTerm.set(term)
      }
      
      // 如果收到有效心跳，转为Follower
      if (currentState.get() != Follower) {
        currentState.set(Follower)
        publishClusterEvent(NodeBecameFollower(
          nodeId = nodeId,
          term = term,
          newLeader = leaderId
        ))
      }
      
      currentLeader = Some(leaderId)
      
      // 重置选举超时
      scheduleElectionTimeout()
    }
  }
  
  /**
   * 获取Master状态快照
   */
  def getMasterStateSnapshot: Option[MasterStateSnapshot] = masterState
  
  /**
   * 设置Master状态快照（用于状态迁移）
   */
  def setMasterStateSnapshot(snapshot: MasterStateSnapshot): Unit = {
    masterState = Some(snapshot)
  }
  
  /**
   * 迁移状态到新Leader
   */
  def migrateStateTo(newLeaderId: String): Future[Unit] = {
    if (masterState.isDefined) {
      val snapshot = masterState.get
      logger.info(s"Migrating state to new leader: $newLeaderId")
      
      publishClusterEvent(MasterStateMigrationStarted(
        fromNode = nodeId,
        toNode = newLeaderId,
        stateSize = snapshot.estimateSize()
      ))
      
      // TODO: 实现实际的状态传输逻辑
      Future {
        Thread.sleep(100) // 模拟迁移时间
        
        publishClusterEvent(MasterStateMigrationCompleted(
          fromNode = nodeId,
          toNode = newLeaderId,
          migratedItems = snapshot.workerStates.size + snapshot.taskStates.size,
          duration = 100L
        ))
      }.recover {
        case ex: Exception =>
          publishClusterEvent(MasterStateMigrationFailed(
            fromNode = nodeId,
            toNode = newLeaderId,
            error = ex.getMessage
          ))
          throw ex
      }
    } else {
      Future.successful(())
    }
  }
  
  // ========== 辅助方法 ==========
  
  def isLeader: Boolean = currentState.get() == Leader
  def isFollower: Boolean = currentState.get() == Follower
  def isCandidate: Boolean = currentState.get() == Candidate
  def getCurrentLeader: Option[String] = currentLeader
  def getCurrentTerm: Long = currentTerm.get()
  def getClusterSize: Int = masterNodes.size
  
  private def handleMemberUp(member: akka.cluster.Member): Unit = {
    val memberId = member.uniqueAddress.address.toString
    logger.info(s"Member up: $memberId with roles: ${member.roles}")
    
    clusterNodes += memberId -> ClusterNode(
      nodeId = memberId,
      address = member.address,
      roles = member.roles
    )
    
    if (member.roles.contains("master")) {
      masterNodes += memberId
    }
    
    publishClusterEvent(ClusterNodeUp(
      nodeId = memberId,
      nodeAddress = member.address.toString,
      nodeRoles = member.roles
    ))
  }
  
  private def handleMemberRemoved(member: akka.cluster.Member, reason: String): Unit = {
    val memberId = member.uniqueAddress.address.toString
    logger.info(s"Member removed: $memberId, reason: $reason")
    
    clusterNodes -= memberId
    masterNodes -= memberId
    
    publishClusterEvent(ClusterNodeDown(
      nodeId = memberId,
      nodeAddress = member.address.toString,
      reason = reason
    ))
    
    // 如果是Leader离开，触发新的选举
    if (currentLeader.contains(memberId)) {
      currentLeader = None
      if (masterNodes.nonEmpty && masterNodes.contains(nodeId)) {
        scheduleElectionTimeout(immediately = true)
      }
    }
  }
  
  private def handleMemberUnreachable(member: akka.cluster.Member): Unit = {
    val memberId = member.uniqueAddress.address.toString
    logger.warn(s"Member unreachable: $memberId")
    
    clusterNodes.get(memberId).foreach { node =>
      clusterNodes += memberId -> node.copy(isReachable = false)
    }
    
    publishClusterEvent(ClusterNodeUnreachable(
      nodeId = memberId,
      nodeAddress = member.address.toString,
      observedBy = nodeId
    ))
  }
  
  private def handleMemberReachable(member: akka.cluster.Member): Unit = {
    val memberId = member.uniqueAddress.address.toString
    logger.info(s"Member reachable again: $memberId")
    
    clusterNodes.get(memberId).foreach { node =>
      clusterNodes += memberId -> node.copy(isReachable = true, lastSeen = Instant.now())
    }
    
    publishClusterEvent(ClusterNodeReachable(
      nodeId = memberId,
      nodeAddress = member.address.toString,
      observedBy = nodeId
    ))
  }
  
  private def handleLeaderChanged(leader: Option[Address]): Unit = {
    logger.info(s"Cluster leader changed to: $leader")
    // Akka Cluster的Leader变化，这里主要用于监控
  }
  
  private def requestVotes(term: Long): Future[Unit] = {
    // TODO: 实现向其他节点请求投票的逻辑
    // 这里需要通过Actor消息或其他方式与其他节点通信
    Future.successful(())
  }
  
  private def checkElectionResult(term: Long): Future[Boolean] = {
    val state = electionState.get()
    if (state.votes.size > masterNodes.size / 2) {
      becomeLeader(term)
      Future.successful(true)
    } else {
      Future.successful(false)
    }
  }
  
  private def scheduleElectionTimeout(immediately: Boolean = false): Unit = {
    if (currentState.get() == Follower) {
      val timeout = if (immediately) 100.millis else {
        val min = electionTimeoutMin.toMillis
        val max = electionTimeoutMax.toMillis
        (min + Random.nextLong() % (max - min)).millis
      }
      
      // TODO: 使用Akka Scheduler调度选举超时
      // scheduler.scheduleOnce(timeout) { startElection() }
    }
  }
  
  private def scheduleHeartbeat(): Unit = {
    if (isLeader) {
      // TODO: 使用Akka Scheduler调度心跳发送
      // scheduler.scheduleOnce(heartbeatInterval) { sendHeartbeat() }
    }
  }
  
  private def publishClusterEvent(event: DomainEvent): Unit = {
    eventBus.publishAsync(event).foreach { _ =>
      logger.debug(s"Published cluster event: ${event.eventType}")
    }
  }
}

/**
 * Master状态快照 - 用于状态迁移
 */
case class MasterStateSnapshot(
  workerStates: Map[String, WorkerState],
  taskStates: Map[String, TaskState],
  timestamp: Instant = Instant.now()
) {
  def estimateSize(): Long = {
    (workerStates.size + taskStates.size) * 1024L // 简单估算
  }
}

/**
 * Worker状态
 */
case class WorkerState(
  workerId: String,
  status: String,
  capacity: Int,
  currentTasks: List[String],
  lastHeartbeat: Instant
)

/**
 * 任务状态
 */
case class TaskState(
  taskId: String,
  status: String,
  assignedWorker: Option[String],
  progress: Double,
  lastUpdate: Instant
)
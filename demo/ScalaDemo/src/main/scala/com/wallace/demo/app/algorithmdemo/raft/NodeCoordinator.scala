package com.wallace.demo.app.algorithmdemo.raft

import akka.actor.{Actor, UnhandledMessage}
import com.wallace.demo.app.common.LoopService

import java.util.UUID
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}

/**
 * Author: biyu.huang
 * Date: 2022/12/23 18:24
 * Description:
 */
class NodeCoordinator(sleepMills: Long, name: String) extends LoopService(sleepMills, name) {
  private val isLeader: AtomicBoolean = new AtomicBoolean(false)
  private val isFollower: AtomicBoolean = new AtomicBoolean(true)
  private val isCandidate: AtomicBoolean = new AtomicBoolean(false)
  private val currentNodeID = "%s-%s".format(name, math.abs(UUID.randomUUID().hashCode()))
  private val currentTerm: AtomicLong = new AtomicLong(System.currentTimeMillis())

  override def handleWork(): Unit = {
    logger.info("current timestamp -> %d".format(System.currentTimeMillis()))

    if (isLeader.get()) logger.info("leader: %s".format(currentNodeID))
    if (isFollower.get()) logger.info("follower: %s".format(currentNodeID))
    if (isCandidate.get()) logger.info("candidate: %s".format(currentNodeID))
  }

  def election(): Unit = {
    if (!isCandidate.get()) isCandidate.set(true)

  }

  class HeartbeatActor() extends Actor {
    override def receive: Receive = {
      case ElectionMessage(term, nodeID) =>
        if (term > currentTerm.get()) {
          ValidVote(term, currentNodeID)
        } else {
          TimeoutVote(term, currentNodeID)
        }
      case ValidVote(term, nodeID) =>
      case TimeoutVote(term, nodeID) =>
      case msg: UnhandledMessage => logger.warn(msg.toString)
    }
  }
}

object NodeCoordinator {
  def apply(sleepMills: Long, name: String): NodeCoordinator = new NodeCoordinator(sleepMills, name)
}

package com.wallace.demo.app.algorithmdemo.raft

import com.wallace.demo.app.algorithmdemo.raft.HeartbeatType.HeartbeatType

/**
 * Author: biyu.huang
 * Date: 2022/12/23 18:48
 * Description:
 */
sealed trait Message

case class ElectionMessage(term: Long, nodeID: String, timeout: Int) extends Message

case class HeartbeatMessage(heartbeatType: HeartbeatType, term: Long, nodeID: String,
  currentTimestamp: Long,
  metadata: Map[String, Any]) extends Message

case class ValidVote(term: Long, nodeID: String) extends Message

case class TimeoutVote(term: Long, nodeID: String) extends Message

case class Entry[K, V](key: K, value: V) {
  def getKey: K = key

  def getValue: V = value
}

object HeartbeatType extends Enumeration {
  type HeartbeatType = Value
  val REGULAR, ELECTION, LEADER = Value
}

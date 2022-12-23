package com.wallace.demo.app.algorithmdemo.raft

/**
 * Author: biyu.huang
 * Date: 2022/12/23 18:48
 * Description:
 */
sealed trait Message

case class ElectionMessage(term: Long, nodeID: String) extends Message

case class HeartbeatMessage(term: Long, nodeID: String, metadata: Map[String, Any]) extends Message

case class ValidVote(term: Long, nodeID: String) extends Message

case class TimeoutVote(term: Long, nodeID: String) extends Message

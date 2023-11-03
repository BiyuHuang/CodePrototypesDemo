package com.hackerforfuture.codeprototypes.dataloader.clusters

import akka.actor.ActorPath

/**
 * Author: biyu.huang
 * Date: 2023/11/1 19:06
 * Description:
 */
sealed trait Message

case object Heartbeat extends Message

case object CheckHeartbeat extends Message

case object Register extends Message

case object RegisterTimeout extends Message

// 自定义消息类型，带有唯一标识ID
case class CustomMessage(id: ActorPath, content: Any) extends Message

case class SlaveActorTerminated(id: ActorPath, reason: String) extends Message

case object StopActor extends Message

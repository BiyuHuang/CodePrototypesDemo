package com.hackerforfuture.codeprototypes.dataloader.clusters

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
case class CustomMessage(id: String, content: Any) extends Message

case class SlaveActorTerminated(id: String, reason: String) extends Message

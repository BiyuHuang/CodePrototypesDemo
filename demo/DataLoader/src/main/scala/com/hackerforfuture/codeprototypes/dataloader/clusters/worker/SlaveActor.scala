package com.hackerforfuture.codeprototypes.dataloader.clusters.worker

import akka.actor.{Actor, ActorLogging, PoisonPill, Props, Terminated}
import com.hackerforfuture.codeprototypes.dataloader.clusters._

import java.util.UUID
import scala.concurrent.duration.DurationInt

/**
 * Author: biyu.huang
 * Date: 2023/11/1 17:16
 * Description:
 */

object SlaveActor {
  def props(master: akka.actor.ActorRef): Props = Props(new SlaveActor(master))
}

class SlaveActor(master: akka.actor.ActorRef) extends Actor with ActorLogging {
  // 在启动时发送注册消息
  override def preStart(): Unit = {
    import context.dispatcher
    context
      .system
      .scheduler
      .scheduleWithFixedDelay(5.seconds, 10.seconds, self, Heartbeat)
    self ! Register
  }

  // 生成唯一标识ID
  val uniqueId: String = UUID.randomUUID().toString

  def receive: Receive = {
    case Heartbeat =>
      // 发送带有唯一标识ID的心跳消息给Master
      master ! Heartbeat
      log.info("Sent heartbeat to master.")

    case Register =>
      // 发送带有唯一标识ID的注册消息给Master
      master ! Register
      log.info("try to register with master.")

    case RegisterTimeout =>
      // 注册超时，重新发送注册消息
      self ! Register

    case otherMessage =>
      // 发送带有唯一标识ID的自定义消息给Master
      val customMessage = CustomMessage(uniqueId, otherMessage)
      master ! customMessage

    case Terminated(master) =>
      log.warning("Master %s terminated. %s is shutting down ...".format(
        master.path.name, self.path.name))
      self ! PoisonPill
  }

  context.watch(master)
}


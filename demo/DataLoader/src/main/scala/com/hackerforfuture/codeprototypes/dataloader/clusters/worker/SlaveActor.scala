package com.hackerforfuture.codeprototypes.dataloader.clusters.worker

import akka.actor.{Actor, ActorLogging, ActorPath, Props, Terminated}
import com.hackerforfuture.codeprototypes.dataloader.clusters._

import scala.concurrent.duration.DurationInt

/**
 * Author: biyu.huang
 * Date: 2023/11/1 17:16
 * Description:
 */

object SlaveActor {
  def props(master: akka.actor.ActorRef): Props = Props(new SlaveActor(master))
}

class SlaveActor(master: akka.actor.ActorRef) extends Actor with ActorLogging with EventHandler {
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
  private final val uniqueID: ActorPath = self.path

  def receive: Receive = {
    // 发送带有唯一标识ID的心跳消息给Master
    case Heartbeat => handleHeartbeatEvent()
    // 发送带有唯一标识ID的注册消息给Master
    case Register => handleRegisterEvent()
    // 注册超时，重新发送注册消息
    case RegisterTimeout => handleRegisterEvent()
    case StopActor => handleStopEvent()
    case Terminated(actorRef) =>
      log.warning("Master %s terminated. %s is shutting down ...".format(
        actorRef.path.name, self.path.name))
      self ! StopActor
    case message =>
      // 发送带有唯一标识ID的自定义消息给Master
      val customMessage = CustomMessage(uniqueID, message)
      master ! customMessage
  }

  override def handleRegisterEvent(): Unit = {
    master ! Register
    log.info("try to register with master.")
    context.watch(master)
  }

  override def handleHeartbeatEvent(): Unit = {
    master ! Heartbeat
    log.info("Sent heartbeat to master.")
  }

  override def handleStopEvent(): Unit = {
    log.info("received StopActor message, shutting down ...")
    context.stop(self)
  }

  override def handleRegisterTimeout(): Unit = {}

  override def handelCheckHeartbeatEvent(): Unit = {}
}


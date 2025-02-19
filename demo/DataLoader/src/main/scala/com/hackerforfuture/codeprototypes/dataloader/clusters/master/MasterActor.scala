package com.hackerforfuture.codeprototypes.dataloader.clusters.master

import akka.actor.{Actor, ActorLogging, ActorPath, Props, Terminated}
import com.hackerforfuture.codeprototypes.dataloader.clusters._
import com.hackerforfuture.codeprototypes.dataloader.common.Using

import scala.collection.mutable
import scala.concurrent.duration._

/**
 * Author: biyu.huang
 * Date: 2023/11/1 17:15
 * Description:
 */
object MasterActor {
  def props: Props = Props[MasterActor]
}

class MasterActor extends Actor with ActorLogging with EventHandler with Using {
  // 存储已注册的SlaveActor的唯一标识ID和最后一次收到心跳的时间戳
  private final val registeredSlaves = mutable.HashMap.empty[ActorPath, Long]

  // 心跳超时时间
  private final val heartbeatTimeout: FiniteDuration = 10.seconds

  private final val initialTimeout: FiniteDuration = 5.seconds

  // 启动时设置定时器
  override def preStart(): Unit = {
    //    context.setReceiveTimeout(initialTimeout)

    import context.dispatcher
    context
      .system
      .scheduler
      .schedule(initialTimeout, heartbeatTimeout, self, CheckHeartbeat)
  }

  override def handleHeartbeatEvent(): Unit = {
    syncableBlock {
      val senderId: ActorPath = sender().path
      val ts: Long = System.currentTimeMillis()
      if (registeredSlaves.contains(senderId)) {
        registeredSlaves.put(senderId, ts)
        log.info(s"[%s] Received heartbeat from slave: ${senderId.name}".format(ts))
      } else {
        registeredSlaves.put(senderId, ts)
        log.info(s"[%s] Registered slave: ${senderId.name}".format(ts))
        context.watch(sender())
      }
    }
  }

  override def handleRegisterEvent(): Unit = handleHeartbeatEvent()

  override def handelCheckHeartbeatEvent(): Unit = {
    val currentTime: Long = System.currentTimeMillis()
    val timedOutSlaves = registeredSlaves.filter {
      case (_, lastHeartbeatTime) =>
        val elapsed = currentTime - lastHeartbeatTime
        elapsed > (heartbeatTimeout.toMillis * 10)
    }

    timedOutSlaves.keys.foreach { id =>
      registeredSlaves -= id
      log.warning(s"Slave $id timed out and unregistered.")
      self ! SlaveActorTerminated(id, "No heartbeat received")
    }
  }

  override def handleStopEvent(): Unit = {
    log.info("received StopActor message, shutting down ...")
    registeredSlaves.foreach {
      case (id, _) =>
        log.info("try to stop %s".format(id.name))
        context.system.actorSelection(id) ! StopActor
    }
    registeredSlaves.clear()
    context.stop(self)
  }

  def receive: Receive = {
    case Heartbeat => handleHeartbeatEvent()
    case Register => handleRegisterEvent()
    case CheckHeartbeat => handelCheckHeartbeatEvent()
    case StopActor => handleStopEvent()
    case CustomMessage(id, content) =>
      if (registeredSlaves.contains(id)) {
        log.info(s"Processing message from slave $id: $content")
        // 处理消息逻辑
      } else {
        log.warning(s"Received message from unregistered slave: $id")
        context.system.actorSelection(id) ! RegisterTimeout
      }
    case SlaveActorTerminated(id, reason) =>
      log.warning(s"Slave $id terminated and unregistered. Reason: $reason")
      registeredSlaves.remove(id)
    case Terminated(slave) =>
      val slaveId = slave.path
      registeredSlaves.remove(slaveId)
      log.warning(s"Slave ${slaveId.name} terminated.")
  }

  override def handleRegisterTimeout(): Unit = {}
}

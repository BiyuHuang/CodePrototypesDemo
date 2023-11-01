package com.hackerforfuture.codeprototypes.dataloader.clusters.master

import akka.actor.{Actor, ActorLogging, Props, Terminated}
import com.hackerforfuture.codeprototypes.dataloader.clusters._

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

class MasterActor extends Actor with ActorLogging {
  // 存储已注册的SlaveActor的唯一标识ID和最后一次收到心跳的时间戳
  private final val registeredSlaves = mutable.HashMap.empty[String, Long]

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
      .scheduleWithFixedDelay(initialTimeout, heartbeatTimeout, self, CheckHeartbeat)
  }

  def receive: Receive = {
    case Heartbeat =>
      val senderId: String = sender().path.name
      val ts: Long = System.currentTimeMillis()
      registeredSlaves.put(senderId, ts)
      log.info(s"[%s] Received heartbeat from slave: $senderId".format(ts))

    case Register =>
      val senderId: String = sender().path.name
      val ts: Long = System.currentTimeMillis()
      registeredSlaves.put(senderId, ts)
      log.info(s"[%s] Registered slave: $senderId".format(ts))
      context.watch(sender())

    case CustomMessage(id, content) =>
      if (registeredSlaves.contains(id)) {
        log.info(s"Processing message from slave $id: $content")
        // 处理消息逻辑
      } else {
        log.warning(s"Received message from unregistered slave: $id")
      }

    case CheckHeartbeat =>
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

    case Terminated(slave) =>
      val slaveId: String = slave.path.name
      // registeredSlaves.remove(slaveId)
      log.warning(s"Slave $slaveId terminated.")

    case SlaveActorTerminated(id, reason) =>
      log.warning(s"Slave $id terminated and unregistered. Reason: $reason")
      registeredSlaves.remove(id)
  }
}

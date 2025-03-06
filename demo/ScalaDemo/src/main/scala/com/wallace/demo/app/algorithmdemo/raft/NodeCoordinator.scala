package com.wallace.demo.app.algorithmdemo.raft

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props, UnhandledMessage}
import com.wallace.demo.app.common.LoopService

import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong, AtomicReference}
import java.util.{Properties, UUID}
import java.security.SecureRandom
import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

/**
 * Author: biyu.huang
 * Date: 2022/12/23 18:24
 * Description:
 */
class NodeCoordinator(sleepMills: Long, name: String, host: String, nodeProps: Properties)
  extends LoopService(sleepMills, name) with ActorSystemSupport with AtomicSupport {
  private final val ACTOR_SYSTEM: ActorSystem = this.builder
    .setHost(host)
    .setPort(nodeProps.get("node.actor.port").toString.toInt)
    .setName(name)
    .build()
  private final val ELECTION_TIMEOUT_MILLS: Long = 200L
  private final val isLeader: AtomicBoolean = new AtomicBoolean(false)
  private final val isFollower: AtomicBoolean = new AtomicBoolean(true)
  private final val isCandidate: AtomicBoolean = new AtomicBoolean(false)
  private final val currentNodeID = "%s-%s".format(host, math.abs(UUID.randomUUID().hashCode()))
  private final val currentTerm: AtomicLong = new AtomicLong(1L)
  private final val actorPathMap: Map[String, String] = {
    val port = nodeProps.get("node.actor.port")
    nodeProps.getProperty("node.actor.list", "").split(",", -1).map(_.trim)
      .map {
        host =>
          host -> s"akka.tcp://$name@$host:$port/node/actor"
      }.toMap
  }
  private final val semaphore: Semaphore = new Semaphore(1, true)
  private final val cacheLeaderHeartbeat = new AtomicReference[Entry[String, Long]]()

  private final val internalActor: ActorRef =
    ACTOR_SYSTEM.actorOf(Props(new InternalActor()), "node-internal-actor")

  override def stop(): Unit = {
    ACTOR_SYSTEM.stop(internalActor)
    ACTOR_SYSTEM.terminate()
    super.stop()
  }

  override def handleWork(): Unit = {
    if (isLeader.get()) logger.info("%s is leader".format(currentNodeID))
    if (isFollower.get()) logger.info("%s is follower".format(currentNodeID))
    if (isCandidate.get()) {
      logger.info("%s is candidate".format(currentNodeID))
      val currentTS: Long = System.currentTimeMillis()
      logger.info("current timestamp -> %d".format(currentTS))
      var ts = System.currentTimeMillis()
      while (ts - currentTS >= ELECTION_TIMEOUT_MILLS) {
        ts = System.currentTimeMillis()
      }
    }
  }

  private def doElection(): Unit = {
    if (!isCandidate.get()) isCandidate.set(true)

  }

  private class InternalActor() extends Actor {
    private val nodeActors: mutable.HashMap[String, ActorSelection] = new mutable.HashMap()

    private val secureRandom: SecureRandom = new SecureRandom()

    private def getMessage: Any = {
      atomicBlock[Any](semaphore) {
        if (isLeader.get()) {
          HeartbeatMessage(HeartbeatType.LEADER, currentTerm.get(),
            currentNodeID, System.currentTimeMillis(), Map.empty)
        }

        if (isCandidate.get()) {
          HeartbeatMessage(HeartbeatType.ELECTION, currentTerm.incrementAndGet(),
            currentNodeID, System.currentTimeMillis(), Map.empty)
        }

        if (isFollower.get()) {
          HeartbeatMessage(HeartbeatType.REGULAR, currentTerm.incrementAndGet(),
            currentNodeID, System.currentTimeMillis(), Map.empty)
        }
      }
    }

    override def preStart(): Unit = {
      logger.info("heartbeat-actor-preStart")
      actorPathMap.foreach {
        case (host, actorPath) =>
          val nodeActor: ActorSelection = context.actorSelection(actorPath)
          nodeActors.put(host, nodeActor)
          nodeActor ! ElectionMessage(currentTerm.get(), currentNodeID, 150 + secureRandom.nextInt(150))
      }
      import context.dispatcher
      context.system.scheduler.schedule(0 millis, 100 millis, new Runnable {
        def run(): Unit = {
          self ! getMessage
          //          if (self.isTerminated) {
          //            throw new RuntimeException("timer active for terminated actor")
          //          }
        }
      })
    }

    override def postStop(): Unit = {
      logger.info("heartbeat-actor-postStop")
      nodeActors.foreach {
        case (host, actorSelection) =>
          logger.info(s"stopping actor selection for $host")
        // actorSelection.tell("stopping", sender())
      }
    }

    override def receive: Receive = {
      case HeartbeatMessage(heartbeatType, term, nodeID, currentTimestamp, metadata) =>
        // todo
        heartbeatType match {
          case HeartbeatType.REGULAR =>
          case HeartbeatType.ELECTION =>
          case HeartbeatType.LEADER =>
            if (cacheLeaderHeartbeat.get() == null) {
              cacheLeaderHeartbeat.set(Entry(nodeID, currentTimestamp))
            } else {
              if (currentTimestamp - cacheLeaderHeartbeat.get().value > 100) {
                doElection()
              } else {
                cacheLeaderHeartbeat.set(Entry(nodeID, currentTimestamp))
              }
            }
        }
        if (metadata.contains("leader") && term >= currentTerm.get()) {
          atomicBlock(semaphore) {
            currentTerm.set(term)
            if (nodeID == currentNodeID) {
              isLeader.set(true)
              isFollower.set(false)
            } else {
              isLeader.set(false)
              isFollower.set(true)
            }
            isCandidate.set(false)
          }
        }
      case ElectionMessage(term, nodeID, timeout) =>
        if (term > currentTerm.get()) {
          sender() ! ValidVote(term, currentNodeID)
        } else {
          Thread.sleep(timeout)
          sender() ! TimeoutVote(term, currentNodeID)
        }
      case ValidVote(term, nodeID) =>
      case TimeoutVote(term, nodeID) =>
      case msg: UnhandledMessage => logger.warn(msg.toString)
    }
  }
}

object NodeCoordinator {
  def apply(sleepMills: Long, name: String, host: String,
    nodeProps: Properties): NodeCoordinator = {
    new NodeCoordinator(sleepMills, host, name, nodeProps)
  }
}

package com.hackerforfuture.codeprototypes.dataloader.clusters.master

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import com.hackerforfuture.codeprototypes.dataloader.clusters.worker.SlaveActor
import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport
import org.scalatest.flatspec.AnyFlatSpec

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, DurationInt}

/**
 * Author: biyu.huang
 * Date: 2023/11/1 18:19
 * Description:
 */
class MasterActorUnitSpec extends AnyFlatSpec with LogSupport {
  "Dev" should "run Akka cluster" in {
    val system: ActorSystem = ActorSystem("Akka-Cluster-System")

    // 创建MasterActor
    val master: ActorRef = system.actorOf(MasterActor.props, "master")

    // 创建多个SlaveActor，并将MasterActor作为参数传递给它们
    system.actorOf(SlaveActor.props(master), "slave1")
    system.actorOf(SlaveActor.props(master), "slave2")
    system.actorOf(SlaveActor.props(master), "slave3")

    // 停止系统的示例代码，可以根据需要进行调整
    // 在这个示例中，我们在10秒后停止系统
    import system.dispatcher
    system.scheduler.scheduleOnce(15.seconds) {
      //      system.actorSelection("/user/*") ! PoisonPill
      master ! PoisonPill
      //      system.terminate()
    }
    Await.ready(system.whenTerminated, Duration(60, TimeUnit.SECONDS))
  }
}

package com.hackerforfuture.codeprototypes.dataloader.clusters.master

import akka.actor.{ActorRef, ActorSystem, Terminated}
import com.hackerforfuture.codeprototypes.dataloader.clusters.StopActor
import com.hackerforfuture.codeprototypes.dataloader.clusters.worker.SlaveActor
import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.flatspec.AnyFlatSpec

import java.util.concurrent.TimeUnit
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{Duration, DurationInt}

/**
 * Author: biyu.huang
 * Date: 2023/11/1 18:19
 * Description:
 */
class MasterActorUnitSpec extends AnyFlatSpec with LogSupport {
  "Dev" should "run Akka cluster" in {
    // 创建自定义配置
    val customConfig: Config = ConfigFactory.parseString(
      """
      akka {
        log-dead-letters-during-shutdown = off
        log-dead-letters = off
      }
    """)
    val system: ActorSystem = ActorSystem("Akka-Cluster-System", customConfig)

    // 创建MasterActor
    val master: ActorRef = system.actorOf(MasterActor.props, "master")

    // 创建多个SlaveActor，并将MasterActor作为参数传递给它们
    val actor1 = system.actorOf(SlaveActor.props(master), "slave1")
    val actor2 = system.actorOf(SlaveActor.props(master), "slave2")
    val actor3 = system.actorOf(SlaveActor.props(master), "slave3")

    // 停止系统的示例代码，可以根据需要进行调整
    // 在这个示例中，我们在10秒后停止系统
    import system.dispatcher
    akka.pattern.after(10.seconds, system.scheduler) {
      actor2 ! StopActor
      Future.unit
    }
    //    actor2 ! StopActor
    val future: Future[Unit] = akka.pattern.after(55.seconds, system.scheduler) {
      system.actorSelection("/user/*") ! StopActor
      system.terminate()
      Future.unit
    }
    Await.result(future, Duration(60, TimeUnit.SECONDS))
  }
}

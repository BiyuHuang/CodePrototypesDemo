package com.wallace.scalademo.multithread

import akka.actor.{Actor, ActorRef}
import com.wallace.common.LogSupport

import scala.collection.mutable.ArrayBuffer

/**
  * Created by Wallace on 2016/10/12.
  * synchronized的用法
  */
class Thread_Test(actor: ActorRef) extends Runnable with LogSupport {
  var exit = false
  val metaData = new ArrayBuffer[String]()
  var metaDataInfo: String = _

  override def run(): Unit = {
    metaDataInfo = s"${Thread.currentThread().getName}, Testing..."
    metaData.append(metaDataInfo)
    if (metaDataInfo.nonEmpty) actor ! metaDataInfo else actor ! ""
    //    while (!exit) {
    //      log.info(s"${Thread.currentThread().getName} is running.")
    //      this.synchronized {
    //        (0 to 5).foreach {
    //          x =>
    //            log.info(Thread.currentThread().getName + " synchronized loop " + x)
    //        }
    //      }
    //    }
  }


}

class WorkerActor extends Actor {

  //val masterActor: ActorRef = context.actorOf(Props[MasterActor], "MasterActor")

  override def receive: Receive = {
    //      case Thread_Test.this.metaData.nonEmpty =>
    //        Thread_Test.this.metaData.foreach {
    //          elem =>
    //            sender() ! elem
    //        }
    case v: String => v
  }
}

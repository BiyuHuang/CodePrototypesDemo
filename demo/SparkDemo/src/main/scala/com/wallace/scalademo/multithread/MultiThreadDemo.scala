package com.wallace.scalademo.multithread

import java.io._

import akka.actor._
import akka.util.Timeout
import com.wallace.common.LogSupport

import scala.concurrent.duration._

/**
  * Created by Wallace on 2016/10/12.
  */
object MultiThreadDemo extends LogSupport {
  implicit val timeout = Timeout(100 seconds)
  val file: File = new File("test_01.csv")
  val bfw: BufferedWriter = if (file.exists()) new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true))) else new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)))
  val printWriter: PrintWriter = new PrintWriter(file, "utf-8")
  val system = ActorSystem("Demo1")
  val actor: ActorRef = system.actorOf(Props(new MasterActor(bfw)), name = "Actor1")

  def main(args: Array[String]): Unit = {
    //    val m1 = new ArrayBuffer[String]()
    //    val m2 = new ArrayBuffer[String]()
    val t1: Thread_Test = new Thread_Test(actor)
    val t2: Thread_Test = new Thread_Test(actor)
    val ta = new Thread(t1, "A")
    val tb = new Thread(t2, "B")
    ta.start()
    tb.start()

    // Thread.sleep(5000)
    //log.info("executing....")

    //    val m = m1 ++ m2
    //    for (elem <- m) {
    //      log.info(elem)
    //    }

    //    Thread.sleep(5000)
    //    t1.exit = true
    //    ta.join()
    //    tb.join()
    //    log.info(s"${ta.getName} is exited.")

    //akka.Main.main(Array(classOf[MasterActor].getName))

    //    val system: ActorSystem = ActorSystem("DemoSystem")
    //    val masterActor = system.actorOf(Props(new MasterActor), "master")
    //    val res: Future[Any] = masterActor ? t1
    //    val temp = Await.result(res, timeout.duration).asInstanceOf[String]
    //
    //    log.info(temp)
  }
}

class MasterActor(fw: BufferedWriter) extends Actor with LogSupport {
  //    override def preStart(): Unit = {
  //      // create the greeter actor
  //      val metaData: ActorRef = context.actorOf(Props[WorkerActor], "workerActor")
  //      // tell it to perform the greeting
  //      metaData ! Thread_Test
  //    }
  val worker: ActorRef = context.actorOf(Props[WorkerActor], "WorkerActor")

  override def receive: Receive = {
    case v: String if v.nonEmpty =>
      log.info(s"received message: $v")
      fw.write(v + "\n")
      fw.flush()
    case _ => log.warn(s"never received message.")
  }
}
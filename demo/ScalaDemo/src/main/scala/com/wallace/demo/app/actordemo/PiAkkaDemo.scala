//package com.wallace.demo.app.actordemo
//
//import akka.actor._
//import akka.pattern.ask
//import akka.util.Timeout
//import akka.routing._
//import com.wallace.demo.app.common.LogSupport
//
//import scala.concurrent.duration._
//import scala.concurrent.{Await, Future}
//import scala.math.random
//
///**
//  * Created by 10192057 on 2017/8/15.
//  */
//sealed trait PiMessage
//
//case object Calculate extends PiMessage
//
//case class Work(times: Int) extends PiMessage
//
//case class Result(value: Int) extends PiMessage
//
//case class PiApproximation(pi: Double, duration: Duration) extends PiMessage
//
//class Worker extends Actor {
//  def testMonteCarlo(times: Int): Int = {
//    var acc = 0
//    for (i <- 0 until times) {
//      val x = random * 2 - 1
//      val y = random * 2 - 1
//      if (x * x + y * y < 1) acc += 1
//    }
//    acc
//  }
//
//  def receive: PartialFunction[Any, Unit] = {
//    case Work(times) =>
//      sender ! Result(testMonteCarlo(times))
//  }
//}
//
//class Master(nrOfWorkers: Int, nrOfMessages: Int, times: Int)
//  extends Actor {
//  var calculateSender: ActorRef = _
//  var acc: Int = 0
//  var nrOfResults: Int = 0
//  val start: Long = System.currentTimeMillis
//
//  val workerRouter: ActorRef = context.actorOf(
//    Props[Worker].withRouter(),
//    name = "workerRouter")
//
//  def receive: PartialFunction[Any, Unit] = {
//    case Calculate =>
//      for (i <- 0 until nrOfMessages) workerRouter ! Work(times)
//      calculateSender = sender
//    case Result(value) =>
//      acc += value
//      nrOfResults += 1
//      if (nrOfResults == nrOfMessages) {
//        val pi = (4.0 * acc) / (nrOfMessages * times)
//        calculateSender ! PiApproximation(pi,
//          (System.currentTimeMillis - start).millis)
//      }
//  }
//}
//
//object PiAkkaDemo extends LogSupport {
//  implicit val timeout = Timeout(100 seconds)
//
//  def main(args: Array[String]): Unit = {
//    if (args.length < 3) {
//      log.error("[PiAkkaDemo] Usage: Pi <nrOfWorkers> <nrOfMessages> <times>")
//      System.exit(1)
//    }
//    val system: ActorSystem = ActorSystem("PiSystem")
//    val master: ActorRef = system.actorOf(Props(new Master(
//      args(0).toInt, args(1).toInt, args(2).toInt)),
//      name = "master")
//    val future: Future[Any] = master ? Calculate
//    val approximationPi = Await.result(future, timeout.duration)
//      .asInstanceOf[PiApproximation]
//    log.info("[PiAkkaDemo] Pi: \t" + approximationPi.pi)
//    log.info("[PiAkkaDemo] Spend: \t" + approximationPi.duration)
//    system.stop(master)
//  }
//}

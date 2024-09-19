/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.actordemo.master_worker

import java.util
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{ConcurrentHashMap, LinkedBlockingQueue}
import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, DeadLetter, Props, UnhandledMessage}
import com.typesafe.config.{Config, ConfigFactory}
import sun.misc.{Signal, SignalHandler}

import java.io.{BufferedReader, InputStream, InputStreamReader}
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Created by 10192057 on 2018/6/20 0020.
 */
class Master(val host: String, val port: Int) extends Actor {
  val id2WorkInfo = new mutable.HashMap[String, WorkerInfo]()
  val workers = new mutable.HashSet[WorkerInfo]()
  val workerActors: mutable.HashSet[ActorSelection] = new mutable.HashSet[ActorSelection]()
  private val SHIFT: AtomicLong = new AtomicLong(0)
  val CHECK_INTERVAL: Long = 6000L

  override def preStart(): Unit = {
    import context.dispatcher
    context.system.scheduler.schedule(0 millis, CHECK_INTERVAL millis, self, CheckTimeOutWorker)
    context.system.scheduler.schedule(0 millis, CHECK_INTERVAL millis, self, ScheduleJob)
  }

  //  override def postStop(): Unit = {
  //    workerActors.foreach {
  //      worker =>
  //        println(s"Shutdown worker#${worker.pathString}")
  //        worker.tell(ShutdownNow, context.system.deadLetters)
  //    }
  //  }

  override def receive: PartialFunction[Any, Unit] = {
    case RegisterWorker(id, memory, cores, workerUrl) =>
      if (!id2WorkInfo.contains(id)) {
        val workerInfo: WorkerInfo = WorkerInfo(id, memory, cores, workerUrl)
        id2WorkInfo.put(id, workerInfo)
        workers += workerInfo
        workerActors += context.actorSelection(workerUrl)
        sender() ! RegisteredWorker(s"akka.tcp://MasterSystem@$host:$port/user/Master")
      } else {
        println(s"Worker $id registered.")
      }
    case HeartBeat(workerId) =>
      if (id2WorkInfo.contains(workerId)) {
        id2WorkInfo(workerId).updateLastHeatTime()
      } else {
        println(s"注册表已丢失Worker#$workerId, 重新注册Worker信息")
        sender() ! RegisterWorker(workerId, -1, -1, "")
      }
    case CheckTimeOutWorker =>
      val cur: Long = System.currentTimeMillis()
      val deadWorker: mutable.HashSet[WorkerInfo] = workers.filter(x => cur - x.lastHeartBeatTime.get() > CHECK_INTERVAL)
      for (w <- deadWorker) {
        id2WorkInfo -= w.id
        workers -= w
        workerActors -= context.actorSelection(w.workerUrl)
      }
      println(s"Worker总数: ${workers.size}")
      println(s"Job总数: ${Master.mQueue.size()}")
      println(s"ScheduleJob总数: ${Master.scheduleQueue.size()}")
    case ScheduleJob =>
      println(s"Start to schedule job.")
      val wActors: Array[ActorSelection] = workerActors.toArray
      val scheduleJob: util.ArrayList[String] = new util.ArrayList[String]()
      Master.mQueue.drainTo(scheduleJob, 15 * wActors.length)
      scheduleJob.addAll(Master.scheduleQueue.values())
      if (wActors.length > 0) {
        if (scheduleJob.size() > 0) {
          scheduleJob.asScala.zipWithIndex.foreach {
            case (s, index) =>
              val job: PersistJob = PersistJob(s)
              wActors((index + SHIFT.get().toInt) % wActors.length) ! job
          }
        } else {
          println("There is no any job.")
          SHIFT.set(0L)
        }
      } else {
        println("There is no any worker.")
      }
    case ReceivedJob(job) =>
      Master.scheduleQueue.remove(job)
      if (SHIFT.get() > workerActors.size) {
        SHIFT.set(0L)
      }
    case FailedReceiveJob(job) =>
      if (Master.scheduleQueue.contains(job)) {
        println(s"$job waiting to be scheduled.")
        SHIFT.getAndIncrement()
      } else {
        Master.scheduleQueue.put(job, job)
      }
    case msg: UnhandledMessage => println(msg.message)

    case DeadLetter(msg, from, to) => to.tell(msg, from)
  }
}

object Master {
  val mQueue: LinkedBlockingQueue[String] = new LinkedBlockingQueue[String]()

  val scheduleQueue: ConcurrentHashMap[String, String] = new ConcurrentHashMap[String, String]()

  private def registerLoggingSignalHandler(): Unit = {
    val jvmSignalHandlers = new ConcurrentHashMap[String, SignalHandler]().asScala
    val handler = new SignalHandler() {
      override def handle(signal: Signal): Unit = {
        println(s"Terminating process due to signal $signal")
        jvmSignalHandlers.get(signal.getName).foreach(_.handle(signal))
      }
    }

    def registerHandler(signalName: String): Unit = {
      val oldHandler = Signal.handle(new Signal(signalName), handler)
      if (oldHandler != null) jvmSignalHandlers.put(signalName, oldHandler)
    }

    if (!System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows")) {
      registerHandler("TERM")
      registerHandler("INT")
      registerHandler("HUP")
    }
  }

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      System.exit(1)
    }
    registerLoggingSignalHandler()
    (0 until 15).foreach {
      i =>
        mQueue.add(s"job_$i")
    }
    val host: String = args(0)
    val port: Int = args(1).toInt
    val confStr: String =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
         |akka.actor.warn-about-java-serializer-usage = "false"
       """.stripMargin
    val conf: Config = ConfigFactory.parseString(confStr)
    //val executionContext: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(5))
    //val actorSystem: ActorSystem = ActorSystem("MasterSystem", Option(conf), None, Option(executionContext))
    val actorSystem: ActorSystem = ActorSystem("MasterSystem", conf)
    val master: ActorRef = actorSystem.actorOf(Props(new Master(host, port)), "Master")


    println(master.path.address.toString)
    Runtime.getRuntime.addShutdownHook(new Thread("Master-Shutdown-Hook") {
      override def run(): Unit = {
        actorSystem.stop(master)
        actorSystem.terminate()
      }
    })
    //Await.ready(actorSystem.whenTerminated, Duration(365, TimeUnit.DAYS))
  }
}

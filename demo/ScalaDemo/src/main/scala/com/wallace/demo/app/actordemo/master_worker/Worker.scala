/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.actordemo.master_worker

import java.util.UUID
import java.util.concurrent.{ConcurrentHashMap, LinkedBlockingQueue, TimeUnit}

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props, UnhandledMessage}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by wallace on 2018/6/21 0021.
  */
class Worker(val host: String, val port: Int, masterHost: String, masterPort: Int) extends Actor {
  private var master: ActorSelection = _

  private val memory: Int = 8
  private val cores: Int = 4
  //Worker唯一标识
  val id_Worker: String = UUID.randomUUID().toString
  //心跳间隔
  val HEATBEAT_INTERVAL: Long = 5000L

  override def preStart(): Unit = {
    println("Worker-preStart")
    master = context.actorSelection(s"akka.tcp://MasterSystem@$masterHost:$masterPort/user/Master")
    master ! RegisterWorker(id_Worker, memory, cores, s"akka.tcp://WorkerSystem@$host:$port/user/Worker")
  }

  override def receive: Receive = {
    case RegisteredWorker(masterUrl) =>
      import context.dispatcher
      master = context.actorSelection(masterUrl)
      context.system.scheduler.schedule(0 millis, HEATBEAT_INTERVAL millis, self, SendHeartBeat)
    case RegisterWorker(workerId, -1, -1, "") =>
      master ! RegisterWorker(workerId, 8, 4, s"akka.tcp://WorkerSystem@$host:$port/user/Worker")
    case SendHeartBeat =>
      println(s"定时发生心跳给${master.toString()}, ReceivedJob总数: ${Worker.wQueue.size()}")
      master ! HeartBeat(id_Worker)
    case PersistJob(job) =>
      if (Worker.wQueue.size() < 8 && Worker.wQueue.add(job)) {
        println(s"Receive Job: $job")
        master ! ReceivedJob(job)
      } else {
        println(s"Failed Receive Job: $job")
        master ! FailedReceiveJob(job)
      }
    case msg: UnhandledMessage => println(msg.message)
  }
}

object Worker {
  val wQueue: LinkedBlockingQueue[String] = new LinkedBlockingQueue[String]()

  val receivedQueue: ConcurrentHashMap[String, String] = new ConcurrentHashMap[String, String]()

  def main(args: Array[String]): Unit = {
    if (args.length < 4) {
      System.exit(1)
    }
    val host: String = args(0)
    val port: Int = args(1).toInt
    val masterHost = args(2)
    val masterPort = args(3).toInt
    val confStr: String =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
         |akka.actor.warn-about-java-serializer-usage = "false"
       """.stripMargin
    val conf: Config = ConfigFactory.parseString(confStr)
    val actorSystem: ActorSystem = ActorSystem("WorkerSystem", conf)
    val workerActor: ActorRef = actorSystem.actorOf(Props(new Worker(host, port, masterHost, masterPort)), "Worker")
    println(workerActor.path.toString)
    Await.ready(actorSystem.whenTerminated, Duration(365, TimeUnit.DAYS))
  }
}

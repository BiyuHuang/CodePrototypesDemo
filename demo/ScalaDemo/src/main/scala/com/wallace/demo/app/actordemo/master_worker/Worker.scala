/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.actordemo.master_worker

import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by wallace on 2018/6/20.
  */
class Worker(masterHost: String, masterPort: Int) extends Actor {
  //保存使用akka-url获得的master,该对象可以发生消息
  var master: ActorSelection = _

  //worker唯一标识
  val id_Worker: String = UUID.randomUUID().toString

  //心跳间隔
  val HEARTBEAT_INTERVAL = 5000

  // 统计次数
  val cnt: AtomicLong = new AtomicLong(0L)

  override def preStart(): Unit = {
    println("worker-preStart")
    //通过masterUrl获得ActorSelectiond对象，该对象可以发生消息
    master = context.actorSelection(s"akka.tcp://MasterSystem@$masterHost:$masterPort/user/Master")
    val memory = 8 //the default of memory is 8G
    val cores = 4 //the default number of cores  is 4
    //向master发送注册的信息
    master ! RegisterWorker(id_Worker, memory, cores)
  }

  override def receive: Receive = {
    case RegisteredWorker(masterUrl) =>
      //处理注册成功的逻辑
      //由于第三个参数是传递一个ActorRef对象，但是目前的master是ActorSelection类型，因此先给自己发送下消息
      import context.dispatcher
      context.system.scheduler.schedule(0 millis, HEARTBEAT_INTERVAL millis, self, SendHeartBeat)
    case SendHeartBeat => //发生心跳。在此可用添加额外的处理逻辑
      cnt.getAndIncrement()
      println(s"第${cnt.get()}次：WorkerID#$id_Worker 定时发生心跳给Master.")
      master ! HeartBeat(id_Worker)
    case RegisterWorker(id, -1, -1) =>
      master ! RegisterWorker(id, memory = 8, cores = 4)
  }

}

object Worker {
  def main(args: Array[String]): Unit = {
    val host = args(0)
    val port = args(1).toInt
    val masterHost = args(2)
    val masterPort = args(3).toInt
    val confStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
         |akka.actor.warn-about-java-serializer-usage = "false"
       """.stripMargin
    val conf = ConfigFactory.parseString(confStr)
    val actorSystem: ActorSystem = ActorSystem("WorkerSystem", conf)
    val master: ActorRef = actorSystem.actorOf(Props(new Worker(masterHost, masterPort)), "Worker")
    //actorSystem.terminate()
    Await.ready(actorSystem.whenTerminated, Duration(365, TimeUnit.DAYS))
  }

}

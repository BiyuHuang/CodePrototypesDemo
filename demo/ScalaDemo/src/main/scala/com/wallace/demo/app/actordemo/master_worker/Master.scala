/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.actordemo.master_worker


import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by wallace on 2018/6/20.
  */
class Master(val host: String, val port: Int) extends Actor {
  //workerId->workerInfo
  val id2WorkInfo = new scala.collection.mutable.HashMap[String, WorkerInfo]

  //为了便于一些额外的逻辑，比如按Worker的剩余可用memory进行排序
  val workers = new scala.collection.mutable.HashSet[WorkerInfo]

  //检查worker是否超时的时间间隔
  val CHECK_INTERVAL = 10000

  //统计worker个数
  val cnt: AtomicLong = new AtomicLong(0L)

  override def preStart(): Unit = {
    //使用schedule必须导入该扩展的隐式变量
    //millis是毫秒的单位，其在包scala.concurrent.duration下
    import context.dispatcher
    context.system.scheduler.schedule(0 millis, CHECK_INTERVAL millis, self, CheckTimeOutWorker)
  }

  override def receive: Receive = {
    case RegisterWorker(id, memory, cores) =>
      //把Worker的注册消息封装到类WorkerInfo中
      if (!id2WorkInfo.contains(id)) {
        //当前workerId没有注册
        val workerInfo = WorkerInfo(id, memory, cores)
        id2WorkInfo.put(id, workerInfo)
        workers += workerInfo
        //这里简单发生Master的url通知worker注册成功
        cnt.getAndIncrement()
        sender ! RegisteredWorker(s"akka.tcp://MasterSystem@$host:$port/user/Master")
      }
    case HeartBeat(workerId) => //处理Worker的心跳
      if (id2WorkInfo.contains(workerId)) {
        id2WorkInfo(workerId).updateLastHeartBeatTime()
      } else {
        sender() ! RegisterWorker(workerId, -1, -1)
      }
    case CheckTimeOutWorker => //定时检测是否有超时的worker并进行处理
      val cur = System.currentTimeMillis
      //过滤出超时的worker
      val deadWorker = workers.filter(x => cur - x.lastHeartBeatTime.get() > CHECK_INTERVAL)
      //从记录删除删除超时的worker
      for (w <- deadWorker) {
        id2WorkInfo -= w.id
        workers -= w
        cnt.getAndDecrement()
      }
      if (workers.size <= 0) {
        println("Warning: No workers.")
      } else {
        println(s"Worker总数： ${cnt.get()}")
        workers.map(x => x.id).zipWithIndex.foreach(println)
      }

  }
}

object Master {
  def main(args: Array[String]): Unit = {
    val host = args(0)
    val port = args(1).toInt
    //构造配置参数值，使用3个双引号可以多行，使用s可以在字符串中使用类似Shell的变量
    val confStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
         |akka.actor.warn-about-java-serializer-usage = "false"
       """.stripMargin
    //通过工厂方法获得一个config对象
    val conf = ConfigFactory.parseString(confStr)
    //初始化一个ActorSystem，其名为MasterSystem
    val actorSystem: ActorSystem = ActorSystem("MasterSystem", conf)
    //使用actorSystem实例化一个名为Master的actor,注意这个名称在Worker连接Master时会用到
    val master: ActorRef = actorSystem.actorOf(Props(new Master(host, port)), "Master")
    //阻塞当前线程直到系统关闭退出
    //    actorSystem.awaitTermination
    //actorSystem.terminate()
    Await.ready(actorSystem.whenTerminated, Duration(365, TimeUnit.DAYS))
  }
}

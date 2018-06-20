/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.actordemo.master_worker

/**
  * Created by wallace on 2018/6/20.
  */

//远程通信可序列化特质,需要远程通信的样本类需继承该特质
trait RemoteMessage extends Serializable

//worker向master发生注册消息的
case class RegisterWorker(id: String, memory: Int, cores: Int) extends RemoteMessage

//master向worker反馈注册成功的信息，这里只简单返回master的url
case class RegisteredWorker(masterUrl: String) extends RemoteMessage

//该伴生对象用于worker本地发生消息给自己
case object SendHeartBeat

//worker发生心跳给master
case class HeartBeat(workerId: String) extends RemoteMessage

//该伴生对象用于master定时检测超时的worker
case object CheckTimeOutWorker
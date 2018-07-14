package com.wallace.demo.app.actordemo.master_worker

/**
  * Created by 10192057 on 2018/6/20 0020.
  */
sealed trait RemoteMessage extends Serializable

case class RegisterWorker(id: String, memory: Int, cores: Int, workerUrl: String) extends RemoteMessage

case class ReRegisterWorker(id: String, memory: Int, cores: Int, workerUrl: String) extends RemoteMessage

case class RegisteredWorker(masterUrl: String) extends RemoteMessage

case class LostWorker(masterUrl: String) extends RemoteMessage

case class HeartBeat(workerId: String) extends RemoteMessage

case class PersistJob(source: String) extends RemoteMessage

case class ReceivedJob(job: String) extends RemoteMessage

case class FailedReceiveJob(job: String) extends RemoteMessage

sealed trait LocalMessage

case object CheckTimeOutWorker extends LocalMessage

case object ScheduleJob extends LocalMessage

case object SendHeartBeat extends LocalMessage




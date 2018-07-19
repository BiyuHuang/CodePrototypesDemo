/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.actordemo.master_worker

import java.util.concurrent.atomic.AtomicLong

/**
  * Created by wallace on 2018/6/20 0020.
  */

case class WorkerInfo(id: String, memory: Int, cores: Int, workerUrl: String = "") {
  val lastHeartBeatTime: AtomicLong = new AtomicLong(0)

  def updateLastHeatTime(): Unit = lastHeartBeatTime.set(System.currentTimeMillis())
}

/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.server.dynamicscan

import java.util.{Timer, TimerTask}

/**
  * Created by wallace on 2018/1/20.
  */
class DataScanService extends Runnable {
  private val timer: Timer = new Timer()

  override def run(): Unit = {
    timer.scheduleAtFixedRate(new DataScanTask, 1000L, 30000L)
  }

  class DataScanTask extends TimerTask {
    override def run(): Unit = {
      //TODO Scan data by fixed rate
      timer match {
        case _: Timer =>

        case _ => throw new NullPointerException("Timer is null.")
      }
    }
  }

}

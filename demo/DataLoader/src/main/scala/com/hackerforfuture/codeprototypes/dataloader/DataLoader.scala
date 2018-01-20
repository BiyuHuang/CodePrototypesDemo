/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader

import java.io.File

import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport
import com.hackerforfuture.codeprototypes.dataloader.services.DataLoaderServer

import scala.util.control.NonFatal

/**
  * Created by wallace on 2018/1/20.
  */
object DataLoader extends LogSupport {

  private var stopSymbol: Boolean = false

  def awaitStopOrSleep(sleepTime: Long = 10000L): Unit = {
    while (!stopSymbol) {
      stopSymbol = new File("stop.txt").exists()
      log.debug(s"StopSymbol: $stopSymbol, Thread sleep $sleepTime ms.")
      Thread.sleep(sleepTime)
    }
    log.debug("DataLoader going to shut down right now ...")
    sys.exit(0)
  }

  def main(args: Array[String]): Unit = {
    try {
      DataLoaderServer.run()
      awaitStopOrSleep()
    } catch {
      case NonFatal(e) => log.error("Failed to execute DataLoader: ", e)
    }
  }
}

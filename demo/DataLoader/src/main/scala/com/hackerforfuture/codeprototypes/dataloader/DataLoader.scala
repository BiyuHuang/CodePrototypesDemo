/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader

import java.util.Locale

import com.hackerforfuture.codeprototypes.dataloader.common.{LogSupport, LoggingSignalHandler}
import com.hackerforfuture.codeprototypes.dataloader.server.DataLoaderServer

import scala.util.control.NonFatal

/**
  * Created by wallace on 2018/1/20.
  */
object DataLoader extends LogSupport {
  private val NAME: String = System.getProperty("os.name").toLowerCase(Locale.ROOT)
  private val IS_WINDOWS: Boolean = NAME.startsWith("windows")
  private val isIbmJdk = System.getProperty("java.vendor").contains("IBM")

  def main(args: Array[String]): Unit = {
    try {
      try {
        if (!IS_WINDOWS && !isIbmJdk) {
          new LoggingSignalHandler().register()
        }
      } catch {
        case e: ReflectiveOperationException =>
          log.warn("Failed to register optional signal handler that logs a message when the process is terminated " +
            s"by a signal. Reason for registration failure is: $e", e)
      }

      // attach shutdown handler to catch terminating signals as well as normal termination
      Runtime.getRuntime.addShutdownHook(new Thread("kafka-shutdown-hook") {
        override def run(): Unit = DataLoaderServer.shutdown()
      })

      DataLoaderServer.startup()
      DataLoaderServer.awaitShutdown()

    } catch {
      case NonFatal(e) =>
        log.error("Failed to run DataLoader", e)
        System.exit(1)
    }
    System.exit(0)
  }
}

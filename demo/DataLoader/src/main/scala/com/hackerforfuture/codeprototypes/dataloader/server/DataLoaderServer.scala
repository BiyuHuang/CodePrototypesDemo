/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.server

import java.util.concurrent.{ExecutorService, Executors}

import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport
import com.hackerforfuture.codeprototypes.dataloader.server.download.DataDownLoadService
import com.hackerforfuture.codeprototypes.dataloader.server.dynamicscan.DataScanService

import scala.util.control.NonFatal

/**
  * Created by wallace on 2018/1/20.
  */
object DataLoaderServer extends LogSupport {
  private val threadPool: ExecutorService = Executors.newFixedThreadPool(3)

  def run(): Unit = {
    try {
      threadPool.execute(new DataScanService())
      threadPool.execute(new DataDownLoadService())
      threadPool.execute(new DataDownLoadService())
    } catch {
      case NonFatal(e) => log.error("Failed to execute service thread", e)
    } finally {
      threadPool.shutdown()
    }
  }
}

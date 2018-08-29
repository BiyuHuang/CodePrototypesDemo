/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.server

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{CountDownLatch, ExecutorService, Executors}

import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport
import com.hackerforfuture.codeprototypes.dataloader.server.download.DataDownLoadService
import com.hackerforfuture.codeprototypes.dataloader.server.dynamicscan.DataScanService

import scala.util.control.NonFatal

/**
  * Created by wallace on 2018/1/20.
  */
object DataLoaderServer extends LogSupport {
  private val threadPool: ExecutorService = Executors.newFixedThreadPool(3)

  private val startupComplete: AtomicBoolean = new AtomicBoolean(false)
  private val isShuttingDown: AtomicBoolean = new AtomicBoolean(false)
  private val isStartingUp: AtomicBoolean = new AtomicBoolean(false)

  private var shutdownLatch: CountDownLatch = new CountDownLatch(1)

  def startup(): Unit = {
    try {
      if (isShuttingDown.get) {
        throw new IllegalStateException("DataLoaderServer is still shutting down, cannot re-start!")
      }

      val canStartup = isStartingUp.compareAndSet(false, true)
      if (canStartup && !startupComplete.get) {
        threadPool.execute(new DataScanService())
        threadPool.execute(new DataDownLoadService())
        threadPool.execute(new DataDownLoadService())
        shutdownLatch = new CountDownLatch(1)
        startupComplete.set(true)
        isStartingUp.set(false)
      }
    } catch {
      case NonFatal(e) =>
        log.error("Failed to execute service thread", e)
        isStartingUp.set(false)
        shutdown()
        throw e
    }
  }

  def shutdown(): Unit = {
    try {
      if (isStartingUp.get) {
        throw new IllegalStateException("DataLoaderServer is still starting up, cannot shut down!")
      }

      if (shutdownLatch.getCount > 0 && isShuttingDown.compareAndSet(false, true)) {
        threadPool.shutdown()
        startupComplete.set(false)
        isShuttingDown.set(false)
        shutdownLatch.countDown()
      }

    } catch {
      case NonFatal(e) =>
        log.error("Fatal error during DataLoaderServer shutdown.", e)
        isShuttingDown.set(false)
        throw e
    }
  }

  def awaitShutdown(): Unit = shutdownLatch.await()
}

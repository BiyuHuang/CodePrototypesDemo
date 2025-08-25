/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.server.download

import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport
import com.hackerforfuture.codeprototypes.dataloader.events._

import java.util.UUID
import scala.util.{Failure, Random, Success, Try}

/**
 * 事件驱动的数据下载服务
 */
class EventDrivenDataDownLoadService(eventBus: DataLoaderEventBus) extends Runnable with LogSupport {

  private var isRunning = true

  override def run(): Unit = {
    logger.info("EventDrivenDataDownLoadService started")
    
    // 订阅下载相关事件
    setupEventSubscriptions()
    
    // 模拟定期下载任务
    while (isRunning) {
      try {
        Thread.sleep(20000) // 每20秒执行一次
        simulateDownloadTask()
      } catch {
        case _: InterruptedException =>
          logger.info("DownloadService interrupted")
          isRunning = false
        case ex: Exception =>
          logger.error("Error in download service", ex)
      }
    }
    
    logger.info("EventDrivenDataDownLoadService stopped")
  }

  /**
   * 设置事件订阅
   */
  private def setupEventSubscriptions(): Unit = {
    val downloadRequestListener = new SimpleEventListener[DataDownloadStarted] {
      override def handleEvent(event: DataDownloadStarted): Unit = {
        handleDownloadRequest(event)
      }
    }
    eventBus.subscribe(downloadRequestListener)
    
    logger.info("Download service event subscriptions setup")
  }

  /**
   * 模拟下载任务
   */
  private def simulateDownloadTask(): Unit = {
    val downloadId = UUID.randomUUID().toString
    val sourceLocation = s"http://server/data/file_${Random.nextInt(1000)}.dat"
    val targetFile = s"/data/downloads/file_${Random.nextInt(1000)}.dat"
    
    // 发布下载开始事件
    eventBus.publishAndWait(DataDownloadStarted(
      downloadId = downloadId,
      sourceLocation = sourceLocation,
      targetFile = targetFile
    ))
    
    // 模拟下载过程
    val downloadResult = Try {
      Thread.sleep(Random.nextInt(2000) + 500) // 0.5-2.5秒
      Random.nextInt(5000000) + 500000L // 模拟传输字节数
    }
    
    downloadResult match {
      case Success(bytesTransferred) =>
        eventBus.publishAndWait(DataDownloadCompleted(
          downloadId = downloadId,
          sourceLocation = sourceLocation,
          targetFile = targetFile,
          bytesTransferred = bytesTransferred
        ))
        logger.info(s"Download completed: $downloadId, $bytesTransferred bytes")
        
      case Failure(ex) =>
        logger.error(s"Download failed: $downloadId", ex)
    }
  }

  /**
   * 处理下载请求事件
   */
  private def handleDownloadRequest(event: DataDownloadStarted): Unit = {
    logger.info(s"Processing download request: ${event.downloadId}")
    // 这里可以添加具体的下载处理逻辑
  }

  def stop(): Unit = {
    isRunning = false
  }
}


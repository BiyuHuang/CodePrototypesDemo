/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.server.upload

import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport
import com.hackerforfuture.codeprototypes.dataloader.events._

import java.util.UUID
import scala.util.{Failure, Random, Success, Try}

/**
 * 事件驱动的数据上传服务
 */
class EventDrivenDataUpLoadService(eventBus: DataLoaderEventBus) extends Runnable with LogSupport {

  private var isRunning = true

  override def run(): Unit = {
    logger.info("EventDrivenDataUpLoadService started")
    
    // 订阅上传相关事件
    setupEventSubscriptions()
    
    // 模拟定期上传任务
    while (isRunning) {
      try {
        Thread.sleep(15000) // 每15秒执行一次
        simulateUploadTask()
      } catch {
        case _: InterruptedException =>
          logger.info("UploadService interrupted")
          isRunning = false
        case ex: Exception =>
          logger.error("Error in upload service", ex)
      }
    }
    
    logger.info("EventDrivenDataUpLoadService stopped")
  }

  /**
   * 设置事件订阅
   */
  private def setupEventSubscriptions(): Unit = {
    val uploadRequestListener = new SimpleEventListener[DataUploadStarted] {
      override def handleEvent(event: DataUploadStarted): Unit = {
        handleUploadRequest(event)
      }
    }
    eventBus.subscribe(uploadRequestListener)
    
    logger.info("Upload service event subscriptions setup")
  }

  /**
   * 模拟上传任务
   */
  private def simulateUploadTask(): Unit = {
    val uploadId = UUID.randomUUID().toString
    val sourceFile = s"/data/source/file_${Random.nextInt(1000)}.dat"
    val targetLocation = s"ftp://server/uploads/file_${Random.nextInt(1000)}.dat"
    
    // 发布上传开始事件
    eventBus.publishAndWait(DataUploadStarted(
      uploadId = uploadId,
      sourceFile = sourceFile,
      targetLocation = targetLocation
    ))
    
    // 模拟上传过程
    val uploadResult = Try {
      Thread.sleep(Random.nextInt(3000) + 1000) // 1-4秒
      Random.nextInt(10000000) + 1000000L // 模拟传输字节数
    }
    
    uploadResult match {
      case Success(bytesTransferred) =>
        eventBus.publishAndWait(DataUploadCompleted(
          uploadId = uploadId,
          sourceFile = sourceFile,
          targetLocation = targetLocation,
          bytesTransferred = bytesTransferred
        ))
        logger.info(s"Upload completed: $uploadId, $bytesTransferred bytes")
        
      case Failure(ex) =>
        logger.error(s"Upload failed: $uploadId", ex)
    }
  }

  /**
   * 处理上传请求事件
   */
  private def handleUploadRequest(event: DataUploadStarted): Unit = {
    logger.info(s"Processing upload request: ${event.uploadId}")
    // 这里可以添加具体的上传处理逻辑
  }

  def stop(): Unit = {
    isRunning = false
  }
}
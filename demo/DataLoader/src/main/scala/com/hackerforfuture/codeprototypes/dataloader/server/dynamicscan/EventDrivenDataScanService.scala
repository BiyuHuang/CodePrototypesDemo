/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.server.dynamicscan

import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport
import com.hackerforfuture.codeprototypes.dataloader.events._

import java.util.UUID
import scala.util.Random

/**
 * 事件驱动的数据扫描服务
 */
class EventDrivenDataScanService(eventBus: DataLoaderEventBus) extends Runnable with LogSupport {

  private var isRunning = true

  override def run(): Unit = {
    logger.info("EventDrivenDataScanService started")
    
    // 订阅扫描相关事件
    setupEventSubscriptions()
    
    // 定期扫描任务
    while (isRunning) {
      try {
        Thread.sleep(30000) // 每30秒执行一次扫描
        performDataScan()
      } catch {
        case _: InterruptedException =>
          logger.info("ScanService interrupted")
          isRunning = false
        case ex: Exception =>
          logger.error("Error in scan service", ex)
      }
    }
    
    logger.info("EventDrivenDataScanService stopped")
  }

  /**
   * 设置事件订阅
   */
  private def setupEventSubscriptions(): Unit = {
    val scanRequestListener = new SimpleEventListener[DataScanStarted] {
      override def handleEvent(event: DataScanStarted): Unit = {
        handleScanRequest(event)
      }
    }
    eventBus.subscribe(scanRequestListener)
    
    logger.info("Scan service event subscriptions setup")
  }

  /**
   * 执行数据扫描
   */
  private def performDataScan(): Unit = {
    val scanId = UUID.randomUUID().toString
    val scanType = "incremental"
    val targetPath = s"/data/source/folder_${Random.nextInt(10)}"
    
    // 发布扫描开始事件
    eventBus.publishAndWait(DataScanStarted(
      scanId = scanId,
      scanType = scanType,
      targetPath = targetPath
    ))
    
    try {
      // 模拟扫描过程
      Thread.sleep(Random.nextInt(2000) + 1000) // 1-3秒
      
      // 模拟发现的数据
      val fileCount = Random.nextInt(50) + 1
      val totalSize = (Random.nextDouble() * 100000000L).toLong + 1000000L
      
      eventBus.publishAndWait(DataDiscovered(
        scanId = scanId,
        fileCount = fileCount,
        totalSize = totalSize
      ))
      
      logger.info(s"Scan completed: $scanId, found $fileCount files ($totalSize bytes)")
      
    } catch {
      case ex: Exception =>
        logger.error(s"Scan failed: $scanId", ex)
    }
  }

  /**
   * 处理扫描请求事件
   */
  private def handleScanRequest(event: DataScanStarted): Unit = {
    logger.info(s"Processing scan request: ${event.scanId} for ${event.targetPath}")
    // 这里可以添加具体的扫描处理逻辑
  }

  def stop(): Unit = {
    isRunning = false
  }
}


/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader

import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport
import com.hackerforfuture.codeprototypes.dataloader.events._
import com.hackerforfuture.codeprototypes.dataloader.server.{EventDrivenDataLoaderServer, SystemStatus}

import java.util.UUID
import scala.util.Random

/**
 * 事件驱动DataLoader应用示例
 *
 * 演示功能：
 * - 启动事件驱动服务器
 * - 模拟任务提交和处理
 * - 展示事件流转和状态变化
 * - 监控系统运行状态
 */
object EventDrivenDataLoaderApp extends LogSupport {

  def main(args: Array[String]): Unit = {
    try {
      logger.info("Starting EventDriven DataLoader Application...")

      // 添加优雅关闭钩子
      Runtime.getRuntime.addShutdownHook(new Thread("event-driven-dataloader-shutdown-hook") {
        override def run(): Unit = {
          logger.info("Shutdown hook triggered, stopping server...")
          EventDrivenDataLoaderServer.shutdown()
        }
      })

      // 启动事件驱动服务器
      startServer()

      // 等待系统稳定
      Thread.sleep(5000)

      // 演示事件驱动功能
      demonstrateEventDrivenFeatures()

      // 监控系统状态
      monitorSystemStatus()

      // 等待关闭信号
      EventDrivenDataLoaderServer.awaitShutdown()

    } catch {
      case ex: Exception =>
        logger.error("Failed to run EventDriven DataLoader Application", ex)
        System.exit(1)
    }

    logger.info("EventDriven DataLoader Application stopped")
    System.exit(0)
  }

  /**
   * 启动事件驱动服务器
   */
  private def startServer(): Unit = {
    logger.info("Starting EventDriven DataLoader Server...")
    EventDrivenDataLoaderServer.startup()
    logger.info("EventDriven DataLoader Server started successfully")
  }

  /**
   * 演示事件驱动功能
   */
  private def demonstrateEventDrivenFeatures(): Unit = {
    logger.info("=== Demonstrating Event-Driven Features ===")

    // 获取事件总线
    EventDrivenDataLoaderServer.getEventBus match {
      case Some(eventBus) =>
        // 1. 演示事件发布和订阅
        demonstrateEventPublishSubscribe(eventBus)

        // 2. 演示任务生命周期事件
        demonstrateTaskLifecycleEvents(eventBus)

        // 3. 演示系统事件监控
        demonstrateSystemEventMonitoring(eventBus)

      case None =>
        logger.error("Event bus not available")
    }
  }

  /**
   * 演示事件发布和订阅
   */
  private def demonstrateEventPublishSubscribe(eventBus: DataLoaderEventBus): Unit = {
    logger.info("--- Demonstrating Event Publish/Subscribe ---")

    // 创建一个自定义事件监听器
    val customListener = new SimpleEventListener[TaskSubmitted] {
      override def handleEvent(event: TaskSubmitted): Unit = {
        logger.info(s"📢 Custom listener received TaskSubmitted: ${event.taskId} (priority: ${event.priority})")
      }
    }

    // 订阅任务提交事件
    eventBus.subscribe(customListener)

    // 发布几个任务提交事件
    (1 to 3).foreach { i =>
      val taskId = UUID.randomUUID().toString
      eventBus.publishAndWait(TaskSubmitted(
        taskId = taskId,
        taskType = "demo-task",
        priority = Random.nextInt(10),
        submittedBy = "demo-app"
      ))
      Thread.sleep(1000)
    }

    logger.info("Event publish/subscribe demonstration completed")
  }

  /**
   * 演示任务生命周期事件
   */
  private def demonstrateTaskLifecycleEvents(eventBus: DataLoaderEventBus): Unit = {
    logger.info("--- Demonstrating Task Lifecycle Events ---")

    val taskId = UUID.randomUUID().toString
    val workerId = "demo-worker"

    // 1. 任务提交
    eventBus.publishAndWait(TaskSubmitted(
      taskId = taskId,
      taskType = "lifecycle-demo",
      priority = 5,
      submittedBy = "demo-app"
    ))
    Thread.sleep(500)

    // 2. 任务分配
    eventBus.publishAndWait(TaskAssigned(
      taskId = taskId,
      workerId = workerId
    ))
    Thread.sleep(500)

    // 3. 任务开始
    eventBus.publishAndWait(TaskStarted(
      taskId = taskId,
      workerId = workerId
    ))
    Thread.sleep(2000) // 模拟任务执行时间

    // 4. 任务完成
    eventBus.publishAndWait(TaskCompleted(
      taskId = taskId,
      workerId = workerId,
      result = SuccessResult("Task completed successfully", Some("demo result"))
    ))

    logger.info(s"Task lifecycle demonstration completed for task: $taskId")
  }

  /**
   * 演示系统事件监控
   */
  private def demonstrateSystemEventMonitoring(eventBus: DataLoaderEventBus): Unit = {
    logger.info("--- Demonstrating System Event Monitoring ---")

    // 创建系统事件监控器
    val systemEventMonitor = new SimpleEventListener[DomainEvent] {
      override def handleEvent(event: DomainEvent): Unit = {
        logger.info(s"🔍 System Monitor: ${event.eventType} at ${event.timestamp}")
      }
    }

    // 注意：这里需要为每种事件类型单独订阅
    // 实际应用中可以创建一个更通用的监控机制

    // 发布一些系统事件进行演示
    eventBus.publishAndWait(ConfigurationUpdated(
      configKey = "demo.setting",
      oldValue = Some("old_value"),
      newValue = "new_value",
      updatedBy = "demo-app"
    ))

    logger.info("System event monitoring demonstration completed")
  }

  /**
   * 监控系统状态
   */
  private def monitorSystemStatus(): Unit = {
    logger.info("=== Starting System Status Monitoring ===")

    val monitoringThread = new Thread("status-monitor") {
      override def run(): Unit = {
        var monitoringCount = 0
        while (monitoringCount < 10 && !Thread.currentThread().isInterrupted) {
          try {
            val status = EventDrivenDataLoaderServer.getSystemStatus
            logSystemStatus(status)

            // 显示事件存储统计
            EventDrivenDataLoaderServer.getEventStore.foreach {
              case inMemoryStore: InMemoryEventStore =>
                val stats = inMemoryStore.getStatistics
                logger.info(s"📊 Event Store Stats: ${stats.totalEvents} events, " +
                  s"${stats.aggregateCount} aggregates, ${stats.eventTypeCount} event types")
              case _ =>
                logger.info("📊 Event Store: Non-memory store active")
            }

            Thread.sleep(10000) // 每10秒监控一次
            monitoringCount += 1
          } catch {
            case _: InterruptedException =>
              logger.info("Status monitoring interrupted")
              return
            case ex: Exception =>
              logger.error("Error in status monitoring", ex)
          }
        }
        logger.info("Status monitoring completed")
      }
    }

    monitoringThread.start()
  }

  /**
   * 记录系统状态
   */
  private def logSystemStatus(status: SystemStatus): Unit = {
    logger.info("=" * 50)
    logger.info("📈 SYSTEM STATUS REPORT")
    logger.info("=" * 50)
    logger.info(s"Running: ${status.isRunning}")
    logger.info(s"Actor System: ${status.actorSystemActive}")
    logger.info(s"Event Bus: ${status.eventBusActive}")
    logger.info(s"Master Actor: ${status.masterActorActive}")
    logger.info(s"Worker Count: ${status.workerCount}")

    status.eventStoreStats.foreach { stats =>
      logger.info(s"Events Stored: ${stats.totalEvents}")
      logger.info(s"Aggregates: ${stats.aggregateCount}")
      logger.info(s"Event Types: ${stats.eventTypeCount}")
      stats.oldestEvent.foreach(time => logger.info(s"Oldest Event: $time"))
      stats.newestEvent.foreach(time => logger.info(s"Newest Event: $time"))
    }
    logger.info("=" * 50)
  }
}
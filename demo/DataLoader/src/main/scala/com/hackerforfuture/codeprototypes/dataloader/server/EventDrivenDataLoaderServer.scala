/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.server

import akka.actor.{ActorRef, ActorSystem}
import com.hackerforfuture.codeprototypes.dataloader.clusters.master.EventDrivenMasterActor
import com.hackerforfuture.codeprototypes.dataloader.clusters.worker.EventDrivenSlaveActor
import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport
import com.hackerforfuture.codeprototypes.dataloader.events._
import com.hackerforfuture.codeprototypes.dataloader.server.download.EventDrivenDataDownLoadService
import com.hackerforfuture.codeprototypes.dataloader.server.dynamicscan.EventDrivenDataScanService
import com.hackerforfuture.codeprototypes.dataloader.server.upload.EventDrivenDataUpLoadService

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{CountDownLatch, ExecutorService, Executors}
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

/**
 * 事件驱动的DataLoader服务器
 * 
 * 功能增强：
 * - 集成事件总线和事件存储
 * - 启动事件驱动的Actor系统
 * - 提供事件监控和管理接口
 * - 支持分布式集群管理
 */
object EventDrivenDataLoaderServer extends LogSupport {
  
  private implicit val executionContext: ExecutionContext = ExecutionContext.global
  
  // 服务状态
  private val startupComplete: AtomicBoolean = new AtomicBoolean(false)
  private val isShuttingDown: AtomicBoolean = new AtomicBoolean(false)
  private val isStartingUp: AtomicBoolean = new AtomicBoolean(false)
  
  private var shutdownLatch: CountDownLatch = new CountDownLatch(1)
  
  // 核心组件
  private var actorSystem: Option[ActorSystem] = None
  private var eventBus: Option[DataLoaderEventBus] = None
  private var eventStore: Option[EventStore] = None
  private var masterActor: Option[ActorRef] = None
  private var workerActors: List[ActorRef] = List.empty
  
  // 服务线程池
  private val threadPool: ExecutorService = Executors.newFixedThreadPool(3)
  
  /**
   * 启动事件驱动服务器
   */
  def startup(): Unit = {
    try {
      if (isShuttingDown.get) {
        throw new IllegalStateException("EventDrivenDataLoaderServer is still shutting down, cannot re-start!")
      }

      val canStartup = isStartingUp.compareAndSet(false, true)
      if (canStartup && !startupComplete.get) {
        
        logger.info("Starting EventDrivenDataLoaderServer...")
        
        // 1. 初始化Actor系统
        initializeActorSystem()
        
        // 2. 初始化事件基础设施
        initializeEventInfrastructure()
        
        // 3. 启动Master Actor
        startMasterActor()
        
        // 4. 启动Worker Actors
        startWorkerActors()
        
        // 5. 启动数据处理服务
        startDataProcessingServices()
        
        // 6. 设置系统事件监听
        setupSystemEventListeners()
        
        shutdownLatch = new CountDownLatch(1)
        startupComplete.set(true)
        isStartingUp.set(false)
        
        logger.info("EventDrivenDataLoaderServer started successfully")
        
        // 发布系统启动事件
        publishSystemStartedEvent()
      }
    } catch {
      case NonFatal(e) =>
        logger.error("Failed to start EventDrivenDataLoaderServer", e)
        isStartingUp.set(false)
        shutdown()
        throw e
    }
  }

  /**
   * 关闭服务器
   */
  def shutdown(): Unit = {
    try {
      if (isStartingUp.get) {
        throw new IllegalStateException("EventDrivenDataLoaderServer is still starting up, cannot shut down!")
      }

      if (shutdownLatch.getCount > 0 && isShuttingDown.compareAndSet(false, true)) {
        logger.info("Shutting down EventDrivenDataLoaderServer...")
        
        // 发布系统关闭事件
        publishSystemShutdownEvent()
        
        // 1. 停止Worker Actors
        stopWorkerActors()
        
        // 2. 停止Master Actor
        stopMasterActor()
        
        // 3. 关闭数据处理服务
        stopDataProcessingServices()
        
        // 4. 关闭事件基础设施
        shutdownEventInfrastructure()
        
        // 5. 关闭Actor系统
        shutdownActorSystem()
        
        startupComplete.set(false)
        isShuttingDown.set(false)
        shutdownLatch.countDown()
        
        logger.info("EventDrivenDataLoaderServer shutdown completed")
      }
    } catch {
      case NonFatal(e) =>
        logger.error("Error during EventDrivenDataLoaderServer shutdown", e)
        isShuttingDown.set(false)
        throw e
    }
  }

  /**
   * 等待关闭完成
   */
  def awaitShutdown(): Unit = shutdownLatch.await()

  /**
   * 初始化Actor系统
   */
  private def initializeActorSystem(): Unit = {
    actorSystem = Some(ActorSystem("DataLoaderSystem"))
    logger.info("Actor system initialized")
  }

  /**
   * 初始化事件基础设施
   */
  private def initializeEventInfrastructure(): Unit = {
    actorSystem.foreach { system =>
      // 创建事件存储
      eventStore = Some(EventStore.inMemory(maxEvents = 50000))
      logger.info("Event store initialized")
      
      // 创建事件总线
      val bus = EventBus.initialize(system)
      eventStore.foreach(bus.withEventStore)
      eventBus = Some(bus)
      logger.info("Event bus initialized")
    }
  }

  /**
   * 启动Master Actor
   */
  private def startMasterActor(): Unit = {
    for {
      system <- actorSystem
      bus <- eventBus
    } {
      masterActor = Some(system.actorOf(EventDrivenMasterActor.props(bus), "master"))
      logger.info("Master actor started")
    }
  }

  /**
   * 启动Worker Actors
   */
  private def startWorkerActors(): Unit = {
    for {
      system <- actorSystem
      bus <- eventBus
      master <- masterActor
    } {
      // 启动多个Worker Actor
      val workerCount = Runtime.getRuntime.availableProcessors()
      workerActors = (1 to workerCount).map { i =>
        system.actorOf(EventDrivenSlaveActor.props(master, bus), s"worker-$i")
      }.toList
      logger.info(s"Started $workerCount worker actors")
    }
  }

  /**
   * 启动数据处理服务
   */
  private def startDataProcessingServices(): Unit = {
    eventBus.foreach { bus =>
      threadPool.execute(new EventDrivenDataScanService(bus))
      threadPool.execute(new EventDrivenDataDownLoadService(bus))
      threadPool.execute(new EventDrivenDataUpLoadService(bus))
      logger.info("Data processing services started")
    }
  }

  /**
   * 设置系统事件监听
   */
  private def setupSystemEventListeners(): Unit = {
    eventBus.foreach { bus =>
      // 监听集群事件
      val clusterEventListener = new SimpleEventListener[WorkerRegistered] {
        override def handleEvent(event: WorkerRegistered): Unit = {
          logger.info(s"Worker registered: ${event.workerId}")
        }
      }
      bus.subscribe(clusterEventListener)
      
      // 监听任务事件
      val taskEventListener = new SimpleEventListener[TaskCompleted] {
        override def handleEvent(event: TaskCompleted): Unit = {
          logger.info(s"Task completed: ${event.taskId} by worker ${event.workerId}")
        }
      }
      bus.subscribe(taskEventListener)
      
      logger.info("System event listeners setup completed")
    }
  }

  /**
   * 发布系统启动事件
   */
  private def publishSystemStartedEvent(): Unit = {
    eventBus.foreach { bus =>
      bus.publishAndWait(SystemStarted(
        systemId = "dataloader-server",
        systemVersion = "2.0.0-event-driven"
      ))
    }
  }

  /**
   * 发布系统关闭事件
   */
  private def publishSystemShutdownEvent(): Unit = {
    eventBus.foreach { bus =>
      bus.publishAndWait(SystemShutdown(
        systemId = "dataloader-server",
        reason = "Normal shutdown"
      ))
    }
  }

  /**
   * 停止Worker Actors
   */
  private def stopWorkerActors(): Unit = {
    workerActors.foreach(_.tell(akka.actor.PoisonPill, ActorRef.noSender))
    workerActors = List.empty
    logger.info("Worker actors stopped")
  }

  /**
   * 停止Master Actor
   */
  private def stopMasterActor(): Unit = {
    masterActor.foreach(_.tell(akka.actor.PoisonPill, ActorRef.noSender))
    masterActor = None
    logger.info("Master actor stopped")
  }

  /**
   * 关闭数据处理服务
   */
  private def stopDataProcessingServices(): Unit = {
    threadPool.shutdown()
    logger.info("Data processing services stopped")
  }

  /**
   * 关闭事件基础设施
   */
  private def shutdownEventInfrastructure(): Unit = {
    EventBus.shutdown()
    eventBus = None
    eventStore = None
    logger.info("Event infrastructure shutdown")
  }

  /**
   * 关闭Actor系统
   */
  private def shutdownActorSystem(): Unit = {
    actorSystem.foreach { system =>
      system.terminate()
      logger.info("Actor system terminated")
    }
    actorSystem = None
  }

  /**
   * 获取系统状态
   */
  def getSystemStatus: SystemStatus = {
    SystemStatus(
      isRunning = startupComplete.get(),
      actorSystemActive = actorSystem.isDefined,
      eventBusActive = eventBus.isDefined,
      masterActorActive = masterActor.isDefined,
      workerCount = workerActors.size,
      eventStoreStats = eventStore.map {
        case inMemory: InMemoryEventStore => Some(inMemory.getStatistics)
        case _ => None
      }.flatten
    )
  }

  /**
   * 获取事件总线实例（用于外部集成）
   */
  def getEventBus: Option[DataLoaderEventBus] = eventBus

  /**
   * 获取事件存储实例（用于查询和分析）
   */
  def getEventStore: Option[EventStore] = eventStore
}

/**
 * 系统状态信息
 */
case class SystemStatus(
  isRunning: Boolean,
  actorSystemActive: Boolean,
  eventBusActive: Boolean,
  masterActorActive: Boolean,
  workerCount: Int,
  eventStoreStats: Option[EventStoreStatistics]
)


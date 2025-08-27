/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.clusters

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
// import akka.management.scaladsl.AkkaManagement // 暂时注释掉，需要额外依赖
import akka.pattern.ask
import akka.util.Timeout
import com.hackerforfuture.codeprototypes.dataloader.clusters.master.{GetClusterStatus, HaMasterActor}
import com.hackerforfuture.codeprototypes.dataloader.clusters.worker.{GetWorkerStatus, HaSlaveActor}
import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport
import com.hackerforfuture.codeprototypes.dataloader.events.{DataLoaderEventBus, EventStore}
import com.typesafe.config.{Config, ConfigFactory}

import java.net.InetAddress
import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * 高可用DataLoader集群启动器
 * 
 * 功能：
 * 1. 根据配置启动不同角色的节点
 * 2. 管理集群生命周期
 * 3. 提供集群状态监控
 * 4. 支持优雅关闭
 */
class HaDataLoaderCluster(
  nodeRoles: Set[String] = Set("master", "worker"),
  port: Int = 0,
  hostname: String = "127.0.0.1"
) extends LogSupport {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val timeout: Timeout = Timeout(10.seconds)

  // 集群组件
  private var actorSystem: Option[ActorSystem] = None
  private var cluster: Option[Cluster] = None
  // private var management: Option[AkkaManagement] = None // 暂时注释掉
  
  // 业务组件
  private var eventBus: Option[DataLoaderEventBus] = None
  private var eventStore: Option[EventStore] = None
  
  // Actor引用
  private var masterActor: Option[ActorRef] = None
  private var workerActors: Map[String, ActorRef] = Map.empty
  
  // 节点信息
  private val nodeId = UUID.randomUUID().toString
  private var isShuttingDown = false

  /**
   * 启动集群节点
   */
  def start(): Future[Unit] = {
    Future {
      logger.info(s"Starting DataLoader HA Cluster node: $nodeId")
      logger.info(s"Node roles: $nodeRoles, hostname: $hostname, port: $port")
      
      // 1. 创建ActorSystem
      val system = createActorSystem()
      actorSystem = Some(system)
      
      // 2. 初始化集群
      val clusterInstance = Cluster(system)
      cluster = Some(clusterInstance)
      
      // 3. 启动Akka Management（可选，用于集群发现）- 暂时注释掉
      // if (shouldEnableManagement()) {
      //   val mgmt = AkkaManagement(system)
      //   management = Some(mgmt)
      //   mgmt.start()
      // }
      
      // 4. 初始化事件基础设施
      initializeEventInfrastructure(system)
      
      // 5. 根据角色启动相应的Actor
      startRoleActors(system)
      
      // 6. 注册关闭钩子
      registerShutdownHook()
      
      // 7. 监听集群事件
      monitorClusterEvents(clusterInstance)
      
      logger.info(s"DataLoader HA Cluster node started successfully: $nodeId")
      
    }.recover {
      case ex =>
        logger.error("Failed to start DataLoader HA Cluster", ex)
        throw ex
    }
  }

  /**
   * 优雅关闭集群节点
   */
  def shutdown(): Future[Unit] = {
    Future {
      if (isShuttingDown) {
        return Future.successful(())
      }
      
      isShuttingDown = true
      logger.info(s"Shutting down DataLoader HA Cluster node: $nodeId")
      
      try {
        // 1. 停止Worker Actors
        workerActors.values.foreach { worker =>
          worker ! PoisonPill
        }
        workerActors = Map.empty
        
        // 2. 停止Master Actor
        masterActor.foreach { master =>
          master ! PoisonPill
        }
        masterActor = None
        
        // 3. 停止Akka Management - 暂时注释掉
        // management.foreach(_.stop())
        
        // 4. 离开集群
        cluster.foreach { c =>
          c.leave(c.selfAddress)
          Thread.sleep(5000) // 等待优雅离开
        }
        
        // 5. 关闭ActorSystem
        actorSystem.foreach { system =>
          system.terminate()
          Thread.sleep(2000)
        }
        
        logger.info(s"DataLoader HA Cluster node shutdown completed: $nodeId")
        
      } catch {
        case ex: Exception =>
          logger.error("Error during cluster shutdown", ex)
          throw ex
      }
    }
  }
  
  /**
   * 获取集群状态
   */
  def getClusterStatus(): Future[ClusterStatusInfo] = {
    cluster match {
      case Some(c) =>
        val members = c.state.members.map(m => MemberInfo(
          address = m.address.toString,
          roles = m.roles,
          status = m.status.toString
        )).toSeq
        
        val unreachable = c.state.unreachable.map(m => MemberInfo(
          address = m.address.toString,
          roles = m.roles,
          status = "Unreachable"
        )).toSeq
        
        Future.successful(ClusterStatusInfo(
          nodeId = nodeId,
          selfAddress = c.selfAddress.toString,
          selfRoles = c.selfRoles,
          leader = c.state.leader.map(_.toString),
          members = members,
          unreachable = unreachable,
          isTerminated = actorSystem.forall(_.whenTerminated.isCompleted)
        ))
        
      case None =>
        Future.failed(new IllegalStateException("Cluster not initialized"))
    }
  }
  
  /**
   * 获取Master状态
   */
  def getMasterStatus(): Future[Option[MasterStatusInfo]] = {
    masterActor match {
      case Some(master) =>
        (master ? GetClusterStatus).mapTo[Any].map {
          case status: com.hackerforfuture.codeprototypes.dataloader.clusters.master.HaClusterStatus =>
            Some(MasterStatusInfo(
              isLeader = status.isLeader,
              currentLeader = status.currentLeader,
              term = status.term,
              clusterSize = status.clusterSize
            ))
          case _ =>
            None
        }.recover {
          case _ => None
        }
      case None =>
        Future.successful(None)
    }
  }
  
  /**
   * 获取Worker状态
   */
  def getWorkerStatuses(): Future[Map[String, WorkerStatusInfo]] = {
    val statusFutures = workerActors.map { case (workerId, worker) =>
      (worker ? GetWorkerStatus).mapTo[Any].map {
        case status: com.hackerforfuture.codeprototypes.dataloader.clusters.worker.WorkerStatusResponse =>
          workerId -> WorkerStatusInfo(
            workerId = status.workerId,
            status = status.status,
            capacity = status.capacity,
            currentTasks = status.currentTasks,
            masterConnected = status.masterConnected
          )
        case _ =>
          workerId -> WorkerStatusInfo(
            workerId = workerId,
            status = "UNKNOWN",
            capacity = 0,
            currentTasks = 0,
            masterConnected = false
          )
      }.recover {
        case _ =>
          workerId -> WorkerStatusInfo(
            workerId = workerId,
            status = "ERROR",
            capacity = 0,
            currentTasks = 0,
            masterConnected = false
          )
      }
    }.toSeq
    
    Future.sequence(statusFutures).map(_.toMap)
  }
  
  /**
   * 添加Worker实例
   */
  def addWorker(workerId: String, capacity: Int = 10): Future[Unit] = {
    actorSystem match {
      case Some(system) if nodeRoles.contains("worker") =>
        eventBus match {
          case Some(bus) =>
            try {
              val worker = system.actorOf(
                HaSlaveActor.props(workerId, capacity, bus),
                s"ha-worker-$workerId"
              )
              workerActors += workerId -> worker
              logger.info(s"Worker added: $workerId with capacity: $capacity")
              Future.successful(())
            } catch {
              case ex: Exception =>
                logger.error(s"Failed to add worker: $workerId", ex)
                Future.failed(ex)
            }
          case None =>
            Future.failed(new IllegalStateException("EventBus not initialized"))
        }
      case _ =>
        Future.failed(new IllegalStateException("Actor system not available or worker role not enabled"))
    }
  }
  
  /**
   * 移除Worker实例
   */
  def removeWorker(workerId: String): Future[Unit] = {
    workerActors.get(workerId) match {
      case Some(worker) =>
        worker ! PoisonPill
        workerActors -= workerId
        logger.info(s"Worker removed: $workerId")
        Future.successful(())
      case None =>
        Future.failed(new IllegalArgumentException(s"Worker not found: $workerId"))
    }
  }
  
  // ========== 私有方法 ==========
  
  /**
   * 创建ActorSystem
   */
  private def createActorSystem(): ActorSystem = {
    val config = createClusterConfig()
    ActorSystem("DataLoaderCluster", config)
  }
  
  /**
   * 创建集群配置
   */
  private def createClusterConfig(): Config = {
    val baseConfig = ConfigFactory.load("cluster.conf")
    
    val overrides = ConfigFactory.parseString(
      s"""
      |akka.remote.artery.canonical.hostname = "$hostname"
      |akka.remote.artery.canonical.port = $port
      |akka.cluster.roles = [${nodeRoles.map('"' + _ + '"').mkString(", ")}]
      |""".stripMargin
    )
    
    overrides.withFallback(baseConfig)
  }
  
  /**
   * 是否启用Management
   */
  private def shouldEnableManagement(): Boolean = {
    // 可以根据配置或环境变量决定
    Try(System.getProperty("akka.management.enable", "false").toBoolean).getOrElse(false)
  }
  
  /**
   * 初始化事件基础设施
   */
  private def initializeEventInfrastructure(system: ActorSystem): Unit = {
    // 创建EventStore
    val store = EventStore.optimizedInMemory(
      maxEvents = 100000,
      maxMemoryMB = 512,
      maxEventSizeKB = 1024
    )
    eventStore = Some(store)
    
    // 创建EventBus
    val bus = new DataLoaderEventBus(system)
    eventBus = Some(bus)
    
    logger.info("Event infrastructure initialized")
  }
  
  /**
   * 根据角色启动相应的Actor
   */
  private def startRoleActors(system: ActorSystem): Unit = {
    eventBus match {
      case Some(bus) =>
        // 启动Master Actor
        if (nodeRoles.contains("master")) {
          val master = system.actorOf(
            HaMasterActor.props(nodeId, bus),
            "ha-master"
          )
          masterActor = Some(master)
          logger.info("Master actor started")
        }
        
        // 启动Worker Actors
        if (nodeRoles.contains("worker")) {
          val workerCount = getConfiguredWorkerCount()
          (1 to workerCount).foreach { i =>
            val workerId = s"$nodeId-worker-$i"
            val worker = system.actorOf(
              HaSlaveActor.props(workerId, 10, bus),
              s"ha-worker-$i"
            )
            workerActors += workerId -> worker
          }
          logger.info(s"$workerCount worker actors started")
        }
        
      case None =>
        throw new IllegalStateException("EventBus not initialized")
    }
  }
  
  /**
   * 获取配置的Worker数量
   */
  private def getConfiguredWorkerCount(): Int = {
    Try {
      val config = ConfigFactory.load()
      config.getInt("dataloader.cluster.node.worker.count")
    }.getOrElse(2) // 默认2个Worker
  }
  
  /**
   * 监听集群事件
   */
  private def monitorClusterEvents(cluster: Cluster): Unit = {
    cluster.subscribe(
      akka.actor.ActorRef.noSender,
      InitialStateAsEvents,
      classOf[MemberEvent],
      classOf[UnreachableMember],
      classOf[ReachableMember],
      classOf[LeaderChanged]
    )
    
    // TODO: 这里需要一个专门的Actor来处理集群事件
    logger.info("Cluster event monitoring started")
  }
  
  /**
   * 注册关闭钩子
   */
  private def registerShutdownHook(): Unit = {
    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      logger.info("Shutdown hook triggered")
      try {
        val future = shutdown()
        // 等待最多10秒
        Thread.sleep(10000)
      } catch {
        case ex: Exception =>
          logger.error("Error in shutdown hook", ex)
      }
    }))
  }
}

/**
 * HaDataLoaderCluster的伴生对象
 */
object HaDataLoaderCluster extends LogSupport {
  
  /**
   * 启动单个节点
   */
  def startNode(
    roles: Set[String] = Set("master", "worker"),
    port: Int = 0,
    hostname: String = "127.0.0.1"
  ): HaDataLoaderCluster = {
    val cluster = new HaDataLoaderCluster(roles, port, hostname)
    cluster.start()
    cluster
  }
  
  /**
   * 启动Master节点
   */
  def startMasterNode(port: Int = 2551, hostname: String = "127.0.0.1"): HaDataLoaderCluster = {
    startNode(Set("master"), port, hostname)
  }
  
  /**
   * 启动Worker节点
   */
  def startWorkerNode(port: Int = 0, hostname: String = "127.0.0.1"): HaDataLoaderCluster = {
    startNode(Set("worker"), port, hostname)
  }
  
  /**
   * 启动混合节点（Master + Worker）
   */
  def startMixedNode(port: Int = 0, hostname: String = "127.0.0.1"): HaDataLoaderCluster = {
    startNode(Set("master", "worker"), port, hostname)
  }
  
  /**
   * 从命令行参数启动
   */
  def main(args: Array[String]): Unit = {
    val (roles, port, hostname) = parseArgs(args)
    
    logger.info(s"Starting DataLoader HA Cluster with roles: $roles, port: $port, hostname: $hostname")
    
    val cluster = new HaDataLoaderCluster(roles, port, hostname)
    cluster.start().onComplete {
      case Success(_) =>
        logger.info("Cluster started successfully")
      case Failure(ex) =>
        logger.error("Failed to start cluster", ex)
        System.exit(1)
    }(ExecutionContext.global)
    
    // 保持主线程运行
    Thread.currentThread().join()
  }
  
  /**
   * 解析命令行参数
   */
  private def parseArgs(args: Array[String]): (Set[String], Int, String) = {
    var roles = Set("master", "worker")
    var port = 0
    var hostname = "127.0.0.1"
    
    args.sliding(2, 2).foreach {
      case Array("--roles", rolesStr) =>
        roles = rolesStr.split(",").map(_.trim).toSet
      case Array("--port", portStr) =>
        port = portStr.toInt
      case Array("--hostname", hostnameStr) =>
        hostname = hostnameStr
      case Array("--master-only") =>
        roles = Set("master")
      case Array("--worker-only") =>
        roles = Set("worker")
      case _ =>
        // 忽略未知参数
    }
    
    (roles, port, hostname)
  }
}

// ========== 状态信息类 ==========

case class ClusterStatusInfo(
  nodeId: String,
  selfAddress: String,
  selfRoles: Set[String],
  leader: Option[String],
  members: Seq[MemberInfo],
  unreachable: Seq[MemberInfo],
  isTerminated: Boolean
)

case class MemberInfo(
  address: String,
  roles: Set[String],
  status: String
)

case class MasterStatusInfo(
  isLeader: Boolean,
  currentLeader: Option[String],
  term: Long,
  clusterSize: Int
)

case class WorkerStatusInfo(
  workerId: String,
  status: String,
  capacity: Int,
  currentTasks: Int,
  masterConnected: Boolean
)
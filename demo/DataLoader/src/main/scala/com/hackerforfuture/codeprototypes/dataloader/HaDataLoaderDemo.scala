/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader

import com.hackerforfuture.codeprototypes.dataloader.clusters.HaDataLoaderCluster
import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport

import scala.concurrent.ExecutionContext
import scala.io.StdIn
import scala.util.{Failure, Success}

/**
 * 高可用DataLoader集群演示应用
 * 
 * 功能：
 * 1. 启动多节点集群
 * 2. 演示故障转移
 * 3. 监控集群状态
 * 4. 交互式操作
 */
object HaDataLoaderDemo extends LogSupport {
  
  implicit val ec: ExecutionContext = ExecutionContext.global
  
  def main(args: Array[String]): Unit = {
    logger.info("Starting DataLoader HA Cluster Demo")
    
    if (args.length > 0 && args(0) == "interactive") {
      runInteractiveDemo()
    } else {
      runBasicDemo()
    }
  }
  
  /**
   * 基础演示 - 启动3节点集群
   */
  private def runBasicDemo(): Unit = {
    logger.info("Running basic HA cluster demo")
    
    try {
      // 启动第一个节点（种子节点，Master+Worker）
      logger.info("Starting seed node 1...")
      val node1 = HaDataLoaderCluster.startMixedNode(port = 2551, hostname = "127.0.0.1")
      Thread.sleep(5000)
      
      // 启动第二个节点（种子节点，Master+Worker）
      logger.info("Starting seed node 2...")
      val node2 = HaDataLoaderCluster.startMixedNode(port = 2552, hostname = "127.0.0.1")
      Thread.sleep(5000)
      
      // 启动第三个节点（Worker节点）
      logger.info("Starting worker node...")
      val node3 = HaDataLoaderCluster.startWorkerNode(port = 0, hostname = "127.0.0.1")
      Thread.sleep(5000)
      
      // 监控集群状态
      monitorClusterStatus(List(node1, node2, node3))
      
      // 演示故障转移（模拟节点1故障）
      logger.info("Simulating node1 failure...")
      Thread.sleep(10000)
      node1.shutdown()
      
      logger.info("Waiting for failover to complete...")
      Thread.sleep(20000)
      
      // 再次检查集群状态
      monitorClusterStatus(List(node2, node3))
      
      // 演示动态添加Worker
      logger.info("Adding additional worker to node2...")
      node2.addWorker("dynamic-worker-1", 20).onComplete {
        case Success(_) =>
          logger.info("Dynamic worker added successfully")
        case Failure(ex) =>
          logger.error("Failed to add dynamic worker", ex)
      }
      
      Thread.sleep(10000)
      
      // 保持运行
      logger.info("Demo completed. Press Enter to shutdown...")
      StdIn.readLine()
      
      // 清理
      node2.shutdown()
      node3.shutdown()
      
    } catch {
      case ex: Exception =>
        logger.error("Demo failed", ex)
    }
  }
  
  /**
   * 交互式演示
   */
  private def runInteractiveDemo(): Unit = {
    logger.info("Running interactive HA cluster demo")
    
    var clusters: List[HaDataLoaderCluster] = List.empty
    
    try {
      // 启动初始集群
      logger.info("Starting initial cluster...")
      val node1 = HaDataLoaderCluster.startMasterNode(port = 2551)
      val node2 = HaDataLoaderCluster.startWorkerNode(port = 2552)
      clusters = List(node1, node2)
      
      Thread.sleep(5000)
      
      // 交互式命令循环
      var running = true
      while (running) {
        println("\n=== DataLoader HA Cluster Demo ===")
        println("1. Show cluster status")
        println("2. Add worker node")
        println("3. Add worker to existing node")
        println("4. Remove worker from node")
        println("5. Simulate node failure")
        println("6. Show detailed status")
        println("7. Exit")
        print("Enter command (1-7): ")
        
        StdIn.readLine().trim match {
          case "1" =>
            showClusterStatus(clusters)
          case "2" =>
            addWorkerNode(clusters)
          case "3" =>
            addWorkerToNode(clusters)
          case "4" =>
            removeWorkerFromNode(clusters)
          case "5" =>
            simulateNodeFailure(clusters)
          case "6" =>
            showDetailedStatus(clusters)
          case "7" =>
            running = false
          case _ =>
            println("Invalid command")
        }
      }
      
    } catch {
      case ex: Exception =>
        logger.error("Interactive demo failed", ex)
    } finally {
      // 清理所有节点
      clusters.foreach(_.shutdown())
    }
  }
  
  /**
   * 监控集群状态
   */
  private def monitorClusterStatus(clusters: List[HaDataLoaderCluster]): Unit = {
    clusters.zipWithIndex.foreach { case (cluster, index) =>
      cluster.getClusterStatus().onComplete {
        case Success(status) =>
          logger.info(s"Node ${index + 1} status:")
          logger.info(s"  Address: ${status.selfAddress}")
          logger.info(s"  Roles: ${status.selfRoles}")
          logger.info(s"  Leader: ${status.leader.getOrElse("None")}")
          logger.info(s"  Members: ${status.members.size}")
          logger.info(s"  Unreachable: ${status.unreachable.size}")
          
        case Failure(ex) =>
          logger.error(s"Failed to get status for node ${index + 1}", ex)
      }
    }
  }
  
  /**
   * 显示集群状态
   */
  private def showClusterStatus(clusters: List[HaDataLoaderCluster]): Unit = {
    println("\n--- Cluster Status ---")
    
    clusters.zipWithIndex.foreach { case (cluster, index) =>
      cluster.getClusterStatus().onComplete {
        case Success(status) =>
          println(s"Node ${index + 1}:")
          println(s"  ID: ${status.nodeId}")
          println(s"  Address: ${status.selfAddress}")
          println(s"  Roles: ${status.selfRoles.mkString(", ")}")
          println(s"  Leader: ${status.leader.getOrElse("None")}")
          println(s"  Members: ${status.members.size}")
          
          // 显示Master状态
          cluster.getMasterStatus().foreach {
            case Some(masterStatus) =>
              println(s"  Master: Leader=${masterStatus.isLeader}, Term=${masterStatus.term}")
            case None =>
              println(s"  Master: Not available")
          }
          
          // 显示Worker状态
          cluster.getWorkerStatuses().foreach { workerStatuses =>
            println(s"  Workers: ${workerStatuses.size}")
            workerStatuses.foreach { case (workerId, workerStatus) =>
              println(s"    $workerId: ${workerStatus.status} (${workerStatus.currentTasks}/${workerStatus.capacity})")
            }
          }
          
        case Failure(ex) =>
          println(s"Node ${index + 1}: ERROR - ${ex.getMessage}")
      }
    }
    
    Thread.sleep(2000) // 等待异步输出完成
  }
  
  /**
   * 添加Worker节点
   */
  private def addWorkerNode(clusters: List[HaDataLoaderCluster]): Unit = {
    try {
      println("Adding new worker node...")
      val newWorker = HaDataLoaderCluster.startWorkerNode()
      clusters :+ newWorker
      println("Worker node added successfully")
    } catch {
      case ex: Exception =>
        println(s"Failed to add worker node: ${ex.getMessage}")
    }
  }
  
  /**
   * 向现有节点添加Worker
   */
  private def addWorkerToNode(clusters: List[HaDataLoaderCluster]): Unit = {
    if (clusters.nonEmpty) {
      print("Enter node index (1-based): ")
      try {
        val nodeIndex = StdIn.readLine().trim.toInt - 1
        if (nodeIndex >= 0 && nodeIndex < clusters.size) {
          print("Enter worker capacity (default 10): ")
          val capacity = StdIn.readLine().trim match {
            case "" => 10
            case cap => cap.toInt
          }
          
          val workerId = s"interactive-worker-${System.currentTimeMillis()}"
          clusters(nodeIndex).addWorker(workerId, capacity).onComplete {
            case Success(_) =>
              println(s"Worker $workerId added to node ${nodeIndex + 1}")
            case Failure(ex) =>
              println(s"Failed to add worker: ${ex.getMessage}")
          }
        } else {
          println("Invalid node index")
        }
      } catch {
        case _: NumberFormatException =>
          println("Invalid number format")
      }
    } else {
      println("No nodes available")
    }
  }
  
  /**
   * 从节点移除Worker
   */
  private def removeWorkerFromNode(clusters: List[HaDataLoaderCluster]): Unit = {
    if (clusters.nonEmpty) {
      print("Enter node index (1-based): ")
      try {
        val nodeIndex = StdIn.readLine().trim.toInt - 1
        if (nodeIndex >= 0 && nodeIndex < clusters.size) {
          print("Enter worker ID: ")
          val workerId = StdIn.readLine().trim
          
          clusters(nodeIndex).removeWorker(workerId).onComplete {
            case Success(_) =>
              println(s"Worker $workerId removed from node ${nodeIndex + 1}")
            case Failure(ex) =>
              println(s"Failed to remove worker: ${ex.getMessage}")
          }
        } else {
          println("Invalid node index")
        }
      } catch {
        case _: NumberFormatException =>
          println("Invalid number format")
      }
    } else {
      println("No nodes available")
    }
  }
  
  /**
   * 模拟节点故障
   */
  private def simulateNodeFailure(clusters: List[HaDataLoaderCluster]): Unit = {
    if (clusters.nonEmpty) {
      print("Enter node index to shutdown (1-based): ")
      try {
        val nodeIndex = StdIn.readLine().trim.toInt - 1
        if (nodeIndex >= 0 && nodeIndex < clusters.size) {
          println(s"Shutting down node ${nodeIndex + 1}...")
          clusters(nodeIndex).shutdown()
          println("Node shutdown initiated. Check cluster status for failover progress.")
        } else {
          println("Invalid node index")
        }
      } catch {
        case _: NumberFormatException =>
          println("Invalid number format")
      }
    } else {
      println("No nodes available")
    }
  }
  
  /**
   * 显示详细状态
   */
  private def showDetailedStatus(clusters: List[HaDataLoaderCluster]): Unit = {
    println("\n--- Detailed Cluster Status ---")
    
    clusters.zipWithIndex.foreach { case (cluster, index) =>
      println(s"\nNode ${index + 1} Details:")
      
      // 集群状态
      cluster.getClusterStatus().foreach { status =>
        println(s"Cluster Status:")
        println(s"  Node ID: ${status.nodeId}")
        println(s"  Self Address: ${status.selfAddress}")
        println(s"  Self Roles: ${status.selfRoles.mkString(", ")}")
        println(s"  Leader: ${status.leader.getOrElse("None")}")
        println(s"  Is Terminated: ${status.isTerminated}")
        
        println(s"Members (${status.members.size}):")
        status.members.foreach { member =>
          println(s"    ${member.address} [${member.roles.mkString(", ")}] - ${member.status}")
        }
        
        if (status.unreachable.nonEmpty) {
          println(s"Unreachable (${status.unreachable.size}):")
          status.unreachable.foreach { member =>
            println(s"    ${member.address} [${member.roles.mkString(", ")}]")
          }
        }
      }
      
      // Master状态
      cluster.getMasterStatus().foreach {
        case Some(masterStatus) =>
          println(s"Master Status:")
          println(s"  Is Leader: ${masterStatus.isLeader}")
          println(s"  Current Leader: ${masterStatus.currentLeader.getOrElse("None")}")
          println(s"  Term: ${masterStatus.term}")
          println(s"  Cluster Size: ${masterStatus.clusterSize}")
          
        case None =>
          println(s"Master: Not available on this node")
      }
      
      // Worker状态
      cluster.getWorkerStatuses().foreach { workerStatuses =>
        if (workerStatuses.nonEmpty) {
          println(s"Worker Status (${workerStatuses.size} workers):")
          workerStatuses.foreach { case (workerId, workerStatus) =>
            println(s"  $workerId:")
            println(s"    Status: ${workerStatus.status}")
            println(s"    Capacity: ${workerStatus.capacity}")
            println(s"    Current Tasks: ${workerStatus.currentTasks}")
            println(s"    Master Connected: ${workerStatus.masterConnected}")
          }
        } else {
          println(s"Workers: None")
        }
      }
    }
    
    Thread.sleep(2000) // 等待异步输出完成
  }
}


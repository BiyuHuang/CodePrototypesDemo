/*
 * 内存使用对比测试
 */

package com.hackerforfuture.codeprototypes.dataloader.events

import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

/**
 * 内存使用对比测试
 * 比较原始实现 vs 优化实现的内存效率
 */
object MemoryComparisonTest extends App {
  
  // 测试配置
  val eventCount = 5000
  val testRounds = 3
  
  println("=== 事件存储内存使用对比测试 ===\n")
  
  // 生成测试事件
  def generateTestEvents(count: Int): Seq[DomainEvent] = {
    val aggregateIds = (1 to 10).map(i => s"aggregate-$i") // 模拟聚合ID重复
    val eventTypes = Seq("TaskSubmitted", "TaskStarted", "TaskCompleted", "WorkerRegistered") // 模拟事件类型重复
    
    (1 to count).map { i =>
      TaskSubmitted(
        taskId = s"task-$i",
        taskType = eventTypes(Random.nextInt(eventTypes.length)),
        priority = Random.nextInt(10),
        submittedBy = s"user-${aggregateIds(Random.nextInt(aggregateIds.length))}"
      )
    }
  }
  
  // 测试原始实现
  def testOriginalImplementation(events: Seq[DomainEvent]): Unit = {
    println("--- 原始 InMemoryEventStore 测试 ---")
    val store = new InMemoryEventStore(maxEvents = 10000)
    
    val startTime = System.currentTimeMillis()
    val startMemory = getUsedMemory()
    
    // 存储事件
    events.foreach(store.store)
    
    val endTime = System.currentTimeMillis()
    val endMemory = getUsedMemory()
    
    val stats = store.getStatistics
    
    println(s"存储时间: ${endTime - startTime}ms")
    println(s"内存使用: ${(endMemory - startMemory) / 1024 / 1024}MB")
    println(s"总事件数: ${stats.totalEvents}")
    println(s"聚合数量: ${stats.aggregateCount}")
    println(s"事件类型: ${stats.eventTypeCount}")
    
    // 查询性能测试
    val queryStart = System.currentTimeMillis()
    val aggregateEvents = store.getEventsForAggregate("task-1")
    val typeEvents = store.getEventsByType("TaskSubmitted")
    val latestEvents = store.getLatestEvents(100)
    val queryEnd = System.currentTimeMillis()
    
    println(s"查询时间: ${queryEnd - queryStart}ms")
    println(s"聚合查询结果: ${aggregateEvents.size}个事件")
    println(s"类型查询结果: ${typeEvents.size}个事件")
    println(s"最新事件查询: ${latestEvents.size}个事件")
    println()
  }
  
  // 测试优化实现
  def testOptimizedImplementation(events: Seq[DomainEvent]): Unit = {
    println("--- 优化 OptimizedInMemoryEventStore 测试 ---")
    val store = EventStore.optimizedInMemory(
      maxEvents = 10000,
      maxMemoryMB = 50,
      maxEventSizeKB = 10
    )
    
    val startTime = System.currentTimeMillis()
    val startMemory = getUsedMemory()
    
    // 存储事件
    events.foreach(store.store)
    
    val endTime = System.currentTimeMillis()
    val endMemory = getUsedMemory()
    
    val stats = store.getDetailedStatistics
    
    println(s"存储时间: ${endTime - startTime}ms")
    println(s"内存使用: ${(endMemory - startMemory) / 1024 / 1024}MB")
    println(s"实际内存统计: ${stats.memoryUsageMB}MB")
    println(s"总事件数: ${stats.totalEvents}")
    println(s"热事件数: ${stats.hotEvents}")
    println(s"温事件数: ${stats.warmEvents}")
    println(s"字符串池大小: ${stats.stringPoolSize}")
    println(s"聚合数量: ${stats.aggregateCount}")
    println(s"事件类型: ${stats.eventTypeCount}")
    println(s"平均事件大小: ${stats.averageEventSizeKB}KB")
    println(f"内存效率: ${stats.memoryEfficiency}%.2f events/MB")
    
    // 查询性能测试
    val queryStart = System.currentTimeMillis()
    val aggregateEvents = store.getEventsForAggregate("task-1")
    val typeEvents = store.getEventsByType("TaskSubmitted")
    val latestEvents = store.getLatestEvents(100)
    val queryEnd = System.currentTimeMillis()
    
    println(s"查询时间: ${queryEnd - queryStart}ms")
    println(s"聚合查询结果: ${aggregateEvents.size}个事件")
    println(s"类型查询结果: ${typeEvents.size}个事件")
    println(s"最新事件查询: ${latestEvents.size}个事件")
    println()
  }
  
  // 获取当前JVM内存使用量
  def getUsedMemory(): Long = {
    val runtime = Runtime.getRuntime
    runtime.totalMemory() - runtime.freeMemory()
  }
  
  // 强制垃圾回收
  def forceGC(): Unit = {
    System.gc()
    Thread.sleep(100)
    System.gc()
    Thread.sleep(100)
  }
  
  // 运行测试
  (1 to testRounds).foreach { round =>
    println(s"=== 第 $round 轮测试 ===")
    
    val testEvents = generateTestEvents(eventCount)
    println(s"生成了 ${testEvents.size} 个测试事件\n")
    
    forceGC()
    testOriginalImplementation(testEvents)
    
    forceGC()
    testOptimizedImplementation(testEvents)
    
    println("=" * 50 + "\n")
  }
  
  println("测试完成！")
  
  println("\n=== 优化效果总结 ===")
  println("🚀 空间优化:")
  println("  • 字符串池化减少重复存储")
  println("  • 索引存储位置而非对象引用")
  println("  • 分层存储(热/温数据)")
  println("  • 智能内存管理")
  
  println("\n⚡ 性能优化:")
  println("  • 内存使用监控")
  println("  • 智能清理策略")
  println("  • 事件大小限制")
  println("  • 批量操作优化")
  
  println("\n📊 可观测性:")
  println("  • 详细内存统计")
  println("  • 分层存储状态")
  println("  • 内存效率指标")
  println("  • 实时监控能力")
}
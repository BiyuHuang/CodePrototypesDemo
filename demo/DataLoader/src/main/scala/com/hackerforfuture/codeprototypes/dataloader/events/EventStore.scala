/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.events

import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport

import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * 事件存储接口 - 提供事件持久化和查询能力
 */
trait EventStore {
  /**
   * 存储单个事件
   */
  def store(event: DomainEvent): Unit

  /**
   * 批量存储事件
   */
  def storeAll(events: Seq[DomainEvent]): Unit

  /**
   * 根据聚合ID获取事件流
   */
  def getEventsForAggregate(aggregateId: String): Seq[DomainEvent]

  /**
   * 根据事件类型获取事件
   */
  def getEventsByType(eventType: String): Seq[DomainEvent]

  /**
   * 根据时间范围获取事件
   */
  def getEventsByTimeRange(start: Instant, end: Instant): Seq[DomainEvent]

  /**
   * 获取最新的N个事件
   */
  def getLatestEvents(limit: Int): Seq[DomainEvent]

  /**
   * 根据事件ID获取事件
   */
  def getEventById(eventId: String): Option[DomainEvent]

  /**
   * 清除存储的事件
   */
  def clear(): Unit

  /**
   * 获取存储的事件总数
   */
  def count(): Long
}

/**
 * 内存事件存储实现 - 适用于开发和测试环境
 *
 * 特性：
 * - 线程安全的内存存储
 * - 支持多种查询方式
 * - 事件索引优化
 * - 简单的内存管理
 */
class InMemoryEventStore(maxEvents: Int = 10000)(implicit ec: ExecutionContext)
  extends EventStore with LogSupport {

  // 主存储：按插入顺序存储所有事件
  private val events = mutable.ListBuffer[DomainEvent]()

  // 索引：按聚合ID分组
  private val eventsByAggregate = TrieMap[String, mutable.ListBuffer[DomainEvent]]()

  // 索引：按事件类型分组
  private val eventsByType = TrieMap[String, mutable.ListBuffer[DomainEvent]]()

  // 索引：按事件ID快速查找
  private val eventsById = TrieMap[String, DomainEvent]()

  // 同步锁
  private val lock = new Object

  /**
   * 存储单个事件
   */
  override def store(event: DomainEvent): Unit = lock.synchronized {
    try {
      // 检查是否已存在
      if (eventsById.contains(event.eventId)) {
        logger.warn(s"Event with id ${event.eventId} already exists, skipping")
        return
      }

      // 内存限制检查
      if (events.size >= maxEvents) {
        removeOldestEvents(maxEvents / 10) // 移除10%的最老事件
      }

      // 存储到主列表
      events += event

      // 更新索引
      eventsByAggregate.getOrElseUpdate(event.aggregateId, mutable.ListBuffer[DomainEvent]()) += event
      eventsByType.getOrElseUpdate(event.eventType, mutable.ListBuffer[DomainEvent]()) += event
      eventsById += (event.eventId -> event)

      logger.debug(s"Stored event: ${event.eventType} with id: ${event.eventId}")
    } catch {
      case ex: Exception =>
        logger.error(s"Failed to store event ${event.eventId}", ex)
        throw ex
    }
  }

  /**
   * 批量存储事件
   */
  override def storeAll(events: Seq[DomainEvent]): Unit = {
    events.foreach(store)
    logger.info(s"Stored ${events.size} events in batch")
  }

  /**
   * 根据聚合ID获取事件流
   */
  override def getEventsForAggregate(aggregateId: String): Seq[DomainEvent] = lock.synchronized {
    eventsByAggregate.getOrElse(aggregateId, mutable.ListBuffer.empty).toSeq.sortBy(_.timestamp)
  }

  /**
   * 根据事件类型获取事件
   */
  override def getEventsByType(eventType: String): Seq[DomainEvent] = lock.synchronized {
    eventsByType.getOrElse(eventType, mutable.ListBuffer.empty).sortBy(_.timestamp)
  }

  /**
   * 根据时间范围获取事件
   */
  override def getEventsByTimeRange(start: Instant, end: Instant): Seq[DomainEvent] = lock.synchronized {
    events.filter { event =>
      event.timestamp.isAfter(start) && event.timestamp.isBefore(end)
    }.sortBy(_.timestamp)
  }

  /**
   * 获取最新的N个事件
   */
  override def getLatestEvents(limit: Int): Seq[DomainEvent] = lock.synchronized {
    events.takeRight(limit).sortBy(_.timestamp).reverse
  }

  /**
   * 根据事件ID获取事件
   */
  override def getEventById(eventId: String): Option[DomainEvent] = {
    eventsById.get(eventId)
  }

  /**
   * 清除存储的事件
   */
  override def clear(): Unit = lock.synchronized {
    events.clear()
    eventsByAggregate.clear()
    eventsByType.clear()
    eventsById.clear()
    logger.info("Cleared all events from store")
  }

  /**
   * 获取存储的事件总数
   */
  override def count(): Long = events.size.toLong

  /**
   * 移除最老的事件以控制内存使用
   */
  private def removeOldestEvents(count: Int): Unit = {
    val toRemove = events.take(count)

    toRemove.foreach { event =>
      // 从主列表移除
      events -= event

      // 从索引移除
      eventsByAggregate.get(event.aggregateId).foreach(_ -= event)
      eventsByType.get(event.eventType).foreach(_ -= event)
      eventsById -= event.eventId
    }

    logger.info(s"Removed $count oldest events to free memory")
  }

  /**
   * 获取存储统计信息
   */
  def getStatistics: EventStoreStatistics = lock.synchronized {
    EventStoreStatistics(
      totalEvents = events.size,
      aggregateCount = eventsByAggregate.size,
      eventTypeCount = eventsByType.size,
      oldestEvent = events.headOption.map(_.timestamp),
      newestEvent = events.lastOption.map(_.timestamp)
    )
  }
}

/**
 * 内存优化的事件存储实现 - 适用于生产环境的内存存储
 *
 * 优化策略：
 * 1. 内存使用监控
 * 2. 智能清理策略
 * 3. 事件大小限制
 * 4. 字符串池化
 * 5. 分层存储
 */
class OptimizedInMemoryEventStore(
  maxEvents: Int = 10000,
  maxMemoryMB: Int = 100,
  maxEventSizeKB: Int = 10
)(implicit ec: ExecutionContext) extends EventStore with LogSupport {

  // 内存使用统计
  private val memoryUsage = new AtomicLong(0L)
  private val eventCount = new AtomicLong(0L)

  // 字符串池化 - 减少重复字符串内存占用
  private val stringPool = TrieMap[String, String]()

  // 分层存储：热数据 + 温数据
  private val hotEvents = mutable.ListBuffer[DomainEvent]() // 最近1000个事件
  private val warmEvents = mutable.ListBuffer[DomainEvent]() // 其余事件

  // 优化的索引结构
  private val eventsByAggregate = TrieMap[String, mutable.ArrayBuffer[Int]]() // 存储索引而非对象
  private val eventsByType = TrieMap[String, mutable.ArrayBuffer[Int]]() // 存储索引而非对象
  private val eventsById = TrieMap[String, Int]() // 存储索引而非对象

  // 同步锁
  private val lock = new Object

  /**
   * 获取池化字符串，减少内存占用
   */
  private def intern(str: String): String = {
    stringPool.getOrElseUpdate(str, str)
  }

  /**
   * 估算事件大小（KB）
   */
  private def estimateEventSize(event: DomainEvent): Int = {
    val baseSize = 200 // 基础对象大小
    val stringSize = Option(event.aggregateId).map(_.length * 2).getOrElse(0) +
      Option(event.eventType).map(_.length * 2).getOrElse(0) +
      Option(event.eventId).map(_.length * 2).getOrElse(0)
    (baseSize + stringSize) / 1024 // 转换为KB
  }

  /**
   * 获取总事件列表（热+温）
   */
  private def getAllEvents: IndexedSeq[DomainEvent] = lock.synchronized {
    (hotEvents ++ warmEvents).toIndexedSeq
  }

  /**
   * 根据索引获取事件
   */
  private def getEventByIndex(index: Int): Option[DomainEvent] = {
    val allEvents = getAllEvents
    if (index >= 0 && index < allEvents.length) Some(allEvents(index)) else None
  }

  override def store(event: DomainEvent): Unit = lock.synchronized {
    try {
      // 1. 事件大小检查
      val eventSize = estimateEventSize(event)
      if (eventSize > maxEventSizeKB) {
        logger.warn(s"Event ${event.eventId} size ${eventSize}KB exceeds limit ${maxEventSizeKB}KB")
        return
      }

      // 2. 去重检查
      val pooledEventId = intern(event.eventId)
      if (eventsById.contains(pooledEventId)) {
        logger.warn(s"Event with id $pooledEventId already exists, skipping")
        return
      }

      // 3. 内存限制检查
      val currentMemoryMB = memoryUsage.get() / (1024 * 1024)
      if (currentMemoryMB > maxMemoryMB) {
        performIntelligentCleanup()
      }

      // 4. 数量限制检查
      if (eventCount.get() >= maxEvents) {
        moveHotToWarm()
        if (eventCount.get() >= maxEvents) {
          removeOldestEvents(maxEvents / 10)
        }
      }

      // 5. 字符串池化处理（直接使用原事件，但对字符串进行池化）
      val pooledAggregateId = intern(event.aggregateId)
      val pooledEventType = intern(event.eventType)

      // 6. 存储到热数据区
      hotEvents += event
      val eventIndex = (hotEvents.size + warmEvents.size) - 1

      // 7. 更新索引（存储索引而非对象引用，使用池化的字符串作为key）
      eventsByAggregate.getOrElseUpdate(pooledAggregateId,
        mutable.ArrayBuffer[Int]()) += eventIndex
      eventsByType.getOrElseUpdate(pooledEventType,
        mutable.ArrayBuffer[Int]()) += eventIndex
      eventsById += (pooledEventId -> eventIndex)

      // 8. 更新统计
      memoryUsage.addAndGet(eventSize * 1024L)
      eventCount.incrementAndGet()

      logger.debug(s"Stored optimized event: ${event.eventType} with id: $pooledEventId")

    } catch {
      case ex: Exception =>
        logger.error(s"Failed to store optimized event ${event.eventId}", ex)
        throw ex
    }
  }

  /**
   * 智能清理策略：基于内存使用量和访问频率
   */
  private def performIntelligentCleanup(): Unit = {
    val startMemory = memoryUsage.get()

    // 1. 清理字符串池中未使用的字符串
    cleanStringPool()

    // 2. 将热数据移到温数据区
    moveHotToWarm()

    // 3. 如果内存仍然紧张，删除最老的温数据
    val currentMemoryMB = memoryUsage.get() / (1024 * 1024)
    if (currentMemoryMB > maxMemoryMB * 0.8) {
      removeOldestEvents(maxEvents / 20) // 删除5%
    }

    val savedMemory = (startMemory - memoryUsage.get()) / (1024 * 1024)
    logger.info(s"Intelligent cleanup saved ${savedMemory}MB memory")
  }

  /**
   * 将热数据移动到温数据区
   */
  private def moveHotToWarm(): Unit = {
    if (hotEvents.size > 1000) {
      val toMove = hotEvents.take(500) // 移动一半
      warmEvents ++= toMove
      hotEvents.remove(0, 500)
      logger.debug(s"Moved ${toMove.size} events from hot to warm storage")
    }
  }

  /**
   * 清理未使用的字符串池
   */
  private def cleanStringPool(): Unit = {
    val usedStrings = mutable.Set[String]()
    getAllEvents.foreach { event =>
      usedStrings += event.aggregateId
      usedStrings += event.eventType
      usedStrings += event.eventId
    }

    val before = stringPool.size
    stringPool.retain((k, _) => usedStrings.contains(k))
    val after = stringPool.size

    if (before > after) {
      logger.debug(s"Cleaned string pool: ${before - after} unused strings removed")
    }
  }

  private def removeOldestEvents(count: Int): Unit = {
    // 从温数据区开始删除
    val toRemoveFromWarm = math.min(count, warmEvents.size)
    if (toRemoveFromWarm > 0) {
      val removed = warmEvents.take(toRemoveFromWarm)
      warmEvents.remove(0, toRemoveFromWarm)

      // 更新统计
      removed.foreach { event =>
        memoryUsage.addAndGet(-estimateEventSize(event) * 1024L)
        eventCount.decrementAndGet()
      }

      logger.info(s"Removed $toRemoveFromWarm oldest events from warm storage")
    }

    // 如果还需要删除更多，从热数据区删除
    val remaining = count - toRemoveFromWarm
    if (remaining > 0 && hotEvents.nonEmpty) {
      val toRemoveFromHot = math.min(remaining, hotEvents.size)
      val removed = hotEvents.take(toRemoveFromHot)
      hotEvents.remove(0, toRemoveFromHot)

      removed.foreach { event =>
        memoryUsage.addAndGet(-estimateEventSize(event) * 1024L)
        eventCount.decrementAndGet()
      }

      logger.info(s"Removed $toRemoveFromHot oldest events from hot storage")
    }

    // 重建索引（因为索引位置改变了）
    rebuildIndexes()
  }

  /**
   * 重建所有索引
   */
  private def rebuildIndexes(): Unit = {
    eventsByAggregate.clear()
    eventsByType.clear()
    eventsById.clear()

    val allEvents = getAllEvents
    allEvents.zipWithIndex.foreach { case (event, index) =>
      // 使用池化的字符串作为key
      val pooledAggregateId = intern(event.aggregateId)
      val pooledEventType = intern(event.eventType)
      val pooledEventId = intern(event.eventId)

      eventsByAggregate.getOrElseUpdate(pooledAggregateId,
        mutable.ArrayBuffer[Int]()) += index
      eventsByType.getOrElseUpdate(pooledEventType,
        mutable.ArrayBuffer[Int]()) += index
      eventsById += (pooledEventId -> index)
    }

    logger.debug("Rebuilt all indexes after cleanup")
  }

  override def storeAll(events: Seq[DomainEvent]): Unit = {
    events.foreach(store)
    logger.info(s"Stored ${events.size} events in optimized batch")
  }

  override def getEventsForAggregate(aggregateId: String): Seq[DomainEvent] = lock.synchronized {
    val pooledId = stringPool.getOrElse(aggregateId, aggregateId)
    val indexes = eventsByAggregate.getOrElse(pooledId, mutable.ArrayBuffer.empty)
    val allEvents = getAllEvents
    indexes.flatMap(i => allEvents.lift(i)).sortBy(_.timestamp)
  }

  override def getEventsByType(eventType: String): Seq[DomainEvent] = lock.synchronized {
    val pooledType = stringPool.getOrElse(eventType, eventType)
    val indexes = eventsByType.getOrElse(pooledType, mutable.ArrayBuffer.empty)
    val allEvents = getAllEvents
    indexes.flatMap(i => allEvents.lift(i)).sortBy(_.timestamp)
  }

  override def getEventsByTimeRange(start: Instant, end: Instant): Seq[DomainEvent] = lock.synchronized {
    getAllEvents.filter { event =>
      event.timestamp.isAfter(start) && event.timestamp.isBefore(end)
    }.sortBy(_.timestamp)
  }

  override def getLatestEvents(limit: Int): Seq[DomainEvent] = lock.synchronized {
    val allEvents = getAllEvents
    allEvents.takeRight(limit).sortBy(_.timestamp).reverse
  }

  override def getEventById(eventId: String): Option[DomainEvent] = {
    val pooledId = stringPool.getOrElse(eventId, eventId)
    eventsById.get(pooledId).flatMap(getEventByIndex)
  }

  override def clear(): Unit = lock.synchronized {
    hotEvents.clear()
    warmEvents.clear()
    eventsByAggregate.clear()
    eventsByType.clear()
    eventsById.clear()
    stringPool.clear()
    memoryUsage.set(0L)
    eventCount.set(0L)
    logger.info("Cleared all optimized events from store")
  }

  override def count(): Long = eventCount.get()

  /**
   * 获取详细的内存统计
   */
  def getDetailedStatistics: OptimizedEventStoreStatistics = lock.synchronized {
    val allEvents = getAllEvents
    OptimizedEventStoreStatistics(
      totalEvents = eventCount.get().toInt,
      hotEvents = hotEvents.size,
      warmEvents = warmEvents.size,
      memoryUsageMB = memoryUsage.get() / (1024 * 1024),
      stringPoolSize = stringPool.size,
      aggregateCount = eventsByAggregate.size,
      eventTypeCount = eventsByType.size,
      oldestEvent = allEvents.headOption.map(_.timestamp),
      newestEvent = allEvents.lastOption.map(_.timestamp),
      averageEventSizeKB =
        if (eventCount.get() > 0) {
          (memoryUsage.get() / eventCount.get() / 1024).toInt
        } else {
          0
        }
    )
  }
}

/**
 * 持久化事件存储实现 - 适用于生产环境
 * 这里提供一个基础框架，实际实现可以基于数据库
 */
class PersistentEventStore(connectionString: String)(implicit ec: ExecutionContext)
  extends EventStore with LogSupport {

  // TODO: 实现基于数据库的持久化存储
  private val inMemoryFallback = new InMemoryEventStore()

  override def store(event: DomainEvent): Unit = {
    Try {
      // TODO: 实现数据库存储逻辑
      // 例如：INSERT INTO events (event_id, event_type, aggregate_id, timestamp, data) VALUES (...)
      logger.debug(s"Persisting event ${event.eventId} to database")
    } match {
      case Success(_) =>
        logger.debug(s"Successfully persisted event ${event.eventId}")
      case Failure(ex) =>
        logger.warn(s"Failed to persist event ${event.eventId}, falling back to memory store", ex)
        inMemoryFallback.store(event)
    }
  }

  override def storeAll(events: Seq[DomainEvent]): Unit = {
    events.foreach(store)
  }

  override def getEventsForAggregate(aggregateId: String): Seq[DomainEvent] = {
    // TODO: 实现数据库查询
    inMemoryFallback.getEventsForAggregate(aggregateId)
  }

  override def getEventsByType(eventType: String): Seq[DomainEvent] = {
    // TODO: 实现数据库查询
    inMemoryFallback.getEventsByType(eventType)
  }

  override def getEventsByTimeRange(start: Instant, end: Instant): Seq[DomainEvent] = {
    // TODO: 实现数据库查询
    inMemoryFallback.getEventsByTimeRange(start, end)
  }

  override def getLatestEvents(limit: Int): Seq[DomainEvent] = {
    // TODO: 实现数据库查询
    inMemoryFallback.getLatestEvents(limit)
  }

  override def getEventById(eventId: String): Option[DomainEvent] = {
    // TODO: 实现数据库查询
    inMemoryFallback.getEventById(eventId)
  }

  override def clear(): Unit = {
    // TODO: 实现数据库清理
    inMemoryFallback.clear()
  }

  override def count(): Long = {
    // TODO: 实现数据库统计
    inMemoryFallback.count()
  }
}

/**
 * 事件存储统计信息
 */
case class EventStoreStatistics(
  totalEvents: Int,
  aggregateCount: Int,
  eventTypeCount: Int,
  oldestEvent: Option[Instant],
  newestEvent: Option[Instant]
)

/**
 * 优化版事件存储统计信息
 */
case class OptimizedEventStoreStatistics(
  totalEvents: Int,
  hotEvents: Int,
  warmEvents: Int,
  memoryUsageMB: Long,
  stringPoolSize: Int,
  aggregateCount: Int,
  eventTypeCount: Int,
  oldestEvent: Option[Instant],
  newestEvent: Option[Instant],
  averageEventSizeKB: Int
) {
  def memoryEfficiency: Double = {
    if (totalEvents > 0) totalEvents.toDouble / memoryUsageMB else 0.0
  }
}

/**
 * 事件存储工厂
 */
object EventStore extends LogSupport {

  /**
   * 创建内存事件存储（原始版本）
   */
  def inMemory(maxEvents: Int = 10000)(implicit ec: ExecutionContext): InMemoryEventStore = {
    logger.info(s"Creating in-memory event store with max $maxEvents events")
    new InMemoryEventStore(maxEvents)
  }

  /**
   * 创建优化的内存事件存储（推荐用于生产环境）
   */
  def optimizedInMemory(
    maxEvents: Int = 10000,
    maxMemoryMB: Int = 100,
    maxEventSizeKB: Int = 10
  )(implicit ec: ExecutionContext): OptimizedInMemoryEventStore = {
    logger.info(s"Creating optimized in-memory event store with max $maxEvents events, ${maxMemoryMB}MB memory limit")
    new OptimizedInMemoryEventStore(maxEvents, maxMemoryMB, maxEventSizeKB)
  }

  /**
   * 创建持久化事件存储
   */
  def persistent(connectionString: String)(implicit ec: ExecutionContext): PersistentEventStore = {
    logger.info(s"Creating persistent event store with connection: $connectionString")
    new PersistentEventStore(connectionString)
  }
}
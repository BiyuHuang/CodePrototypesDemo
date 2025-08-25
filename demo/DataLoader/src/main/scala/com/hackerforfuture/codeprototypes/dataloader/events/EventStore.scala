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
    eventsByType.getOrElse(eventType, mutable.ListBuffer.empty).toSeq.sortBy(_.timestamp)
  }

  /**
   * 根据时间范围获取事件
   */
  override def getEventsByTimeRange(start: Instant, end: Instant): Seq[DomainEvent] = lock.synchronized {
    events.filter { event =>
      event.timestamp.isAfter(start) && event.timestamp.isBefore(end)
    }.toSeq.sortBy(_.timestamp)
  }

  /**
   * 获取最新的N个事件
   */
  override def getLatestEvents(limit: Int): Seq[DomainEvent] = lock.synchronized {
    events.takeRight(limit).toSeq.sortBy(_.timestamp).reverse
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
 * 事件存储工厂
 */
object EventStore extends LogSupport {

  /**
   * 创建内存事件存储
   */
  def inMemory(maxEvents: Int = 10000)(implicit ec: ExecutionContext): InMemoryEventStore = {
    logger.info(s"Creating in-memory event store with max $maxEvents events")
    new InMemoryEventStore(maxEvents)
  }

  /**
   * 创建持久化事件存储
   */
  def persistent(connectionString: String)(implicit ec: ExecutionContext): PersistentEventStore = {
    logger.info(s"Creating persistent event store with connection: $connectionString")
    new PersistentEventStore(connectionString)
  }
}
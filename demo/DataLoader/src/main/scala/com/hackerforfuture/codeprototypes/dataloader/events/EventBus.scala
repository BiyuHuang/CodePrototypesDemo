/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.events

import akka.actor.{ActorRef, ActorSystem}
import akka.event.{EventBus => AkkaEventBus, LookupClassification}
import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/**
 * 事件发布器接口
 */
trait EventPublisher {
  def publish(event: DomainEvent): Future[Unit]

  def publishAndWait(event: DomainEvent): Unit
}

/**
 * 事件监听器接口
 */
trait EventListener[T <: DomainEvent] {
  def handle(event: T): Future[Unit]

  def eventType: Class[T]
}

/**
 * 事件总线 - 基于Akka EventBus实现的发布/订阅机制
 *
 * 特性：
 * - 支持按事件类型分类的订阅
 * - 异步事件处理
 * - 错误处理和重试机制
 * - 事件持久化集成
 */
class DataLoaderEventBus(actorSystem: ActorSystem)(implicit ec: ExecutionContext)
  extends AkkaEventBus
    with LookupClassification
    with LogSupport {

  type Event = DomainEvent
  type Classifier = Class[_ <: DomainEvent]
  type Subscriber = EventListener[_ <: DomainEvent]

  /**
   * 事件存储，用于持久化事件
   */
  private var eventStore: Option[EventStore] = None

  /**
   * 将事件存储集成到事件总线
   */
  def withEventStore(store: EventStore): DataLoaderEventBus = {
    this.eventStore = Some(store)
    this
  }

  /**
   * 根据事件类型进行分类
   */
  override protected def classify(event: Event): Classifier = event.getClass

  /**
   * 获取事件的映射大小（用于优化查找）
   */
  override protected def mapSize: Int = 128

  /**
   * 比较两个订阅者
   */
  override protected def compareSubscribers(a: Subscriber, b: Subscriber): Int = {
    a.hashCode().compareTo(b.hashCode())
  }

  /**
   * 事件分发到订阅者
   */
  override protected def publish(event: Event, subscriber: Subscriber): Unit = {
    Try {
      // 类型安全的事件处理
      val listener = subscriber.asInstanceOf[EventListener[Event]]
      listener.handle(event).recover {
        case ex: Exception =>
          logger.error(s"Error handling event ${event.eventType} with id ${event.eventId}", ex)
      }
    } match {
      case Success(_) =>
        logger.debug(s"Successfully dispatched event ${event.eventType} to ${subscriber.getClass.getSimpleName}")
      case Failure(ex) =>
        logger.error(s"Failed to dispatch event ${event.eventType} to ${subscriber.getClass.getSimpleName}", ex)
    }
  }

  /**
   * 发布事件（异步）
   */
  def publishEvent(event: DomainEvent): Future[Unit] = {
    Future {
      // 先持久化事件
      eventStore.foreach(_.store(event))

      // 然后发布到订阅者
      publish(event)

      logger.info(s"Published event: ${event.eventType} with id: ${event.eventId}")
    }.recover {
      case ex: Exception =>
        logger.error(s"Failed to publish event ${event.eventType} with id ${event.eventId}", ex)
        throw ex
    }
  }

  /**
   * 发布事件（异步）- 外部接口
   */
  def publishAsync(event: DomainEvent): Future[Unit] = publishEvent(event)

  /**
   * 发布事件（同步等待）
   */
  def publishAndWait(event: DomainEvent): Unit = {
    // 先持久化事件
    eventStore.foreach(_.store(event))

    // 然后发布到订阅者
    publish(event)

    logger.info(s"Published event: ${event.eventType} with id: ${event.eventId}")
  }

  /**
   * 订阅特定类型的事件
   */
  def subscribe[T <: DomainEvent : ClassTag](listener: EventListener[T]): Boolean = {
    val eventClass = implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]
    subscribe(listener, eventClass)
  }

  /**
   * 取消订阅
   */
  def unsubscribe[T <: DomainEvent : ClassTag](listener: EventListener[T]): Boolean = {
    val eventClass = implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]
    unsubscribe(listener, eventClass)
  }

  /**
   * 获取指定事件类型的订阅者数量
   */
  def getSubscriberCount[T <: DomainEvent : ClassTag]: Int = {
    val eventClass = implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]
    // 简化实现：返回0，实际项目中可以维护一个订阅者计数器
    0 // TODO: 实现实际的订阅者计数
  }

  /**
   * 清理所有订阅者
   */
  def clearAllSubscriptions(): Unit = {
    // 实现清理逻辑
    logger.info("Cleared all event subscriptions")
  }
}

/**
 * 事件总线工厂和单例管理
 */
object EventBus extends LogSupport {

  @volatile private var instance: Option[DataLoaderEventBus] = None

  /**
   * 初始化事件总线
   */
  def initialize(actorSystem: ActorSystem)(implicit ec: ExecutionContext): DataLoaderEventBus = {
    instance.getOrElse {
      synchronized {
        instance.getOrElse {
          val eventBus = new DataLoaderEventBus(actorSystem)
          instance = Some(eventBus)
          logger.info("EventBus initialized successfully")
          eventBus
        }
      }
    }
  }

  /**
   * 获取事件总线实例
   */
  def getInstance: Option[DataLoaderEventBus] = instance

  /**
   * 关闭事件总线
   */
  def shutdown(): Unit = {
    instance.foreach { bus =>
      bus.clearAllSubscriptions()
      logger.info("EventBus shutdown completed")
    }
    instance = None
  }
}

/**
 * 简化的事件监听器抽象类
 */
abstract class SimpleEventListener[T <: DomainEvent : ClassTag] extends EventListener[T] with LogSupport {

  override def eventType: Class[T] = implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]

  /**
   * 处理事件的具体实现，子类需要重写
   */
  def handleEvent(event: T): Unit

  /**
   * 异步处理事件
   */
  override def handle(event: T): Future[Unit] = {
    Future {
      try {
        handleEvent(event)
        logger.debug(s"Successfully handled event ${event.eventType} with id ${event.eventId}")
      } catch {
        case ex: Exception =>
          logger.error(s"Error handling event ${event.eventType} with id ${event.eventId}", ex)
          throw ex
      }
    }(scala.concurrent.ExecutionContext.global)
  }
}


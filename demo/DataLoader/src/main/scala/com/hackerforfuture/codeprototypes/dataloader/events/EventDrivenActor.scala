/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.events

import akka.actor.{Actor, ActorLogging}
import com.hackerforfuture.codeprototypes.dataloader.clusters.Message

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
 * 事件驱动Actor的基类
 * 
 * 提供以下能力：
 * - 发布领域事件
 * - 订阅和处理事件
 * - 事件与传统消息的桥接
 * - 错误处理和恢复
 */
trait EventDrivenActor extends Actor with ActorLogging {
  
  implicit def executionContext: ExecutionContext = context.dispatcher
  
  /**
   * 事件总线实例（由子类提供或者从外部注入）
   */
  def eventBus: DataLoaderEventBus

  /**
   * Actor的唯一标识，用于事件关联
   */
  def actorId: String = self.path.name

  /**
   * 发布事件的便捷方法
   */
  protected def publishEvent(event: DomainEvent): Unit = {
    try {
      eventBus.publishAndWait(event)
      log.debug(s"Published event: ${event.eventType} with id: ${event.eventId}")
    } catch {
      case ex: Exception =>
        log.error(ex, s"Failed to publish event: ${event.eventType} with id: ${event.eventId}")
    }
  }

  /**
   * 异步发布事件
   */
  protected def publishEventAsync(event: DomainEvent): Unit = {
    eventBus.publishAsync(event).onComplete {
      case Success(_) =>
        log.debug(s"Successfully published event: ${event.eventType} with id: ${event.eventId}")
      case Failure(ex) =>
        log.error(ex, s"Failed to publish event: ${event.eventType} with id: ${event.eventId}")
    }
  }

  /**
   * 订阅事件的便捷方法
   */
  protected def subscribeToEvent[T <: DomainEvent](eventClass: Class[T])(handler: T => Unit): Unit = {
    val listener = new EventListener[T] {
      override def handle(event: T): scala.concurrent.Future[Unit] = {
        scala.concurrent.Future {
          try {
            handler(event)
            log.debug(s"Successfully handled event ${event.eventType} with id ${event.eventId}")
          } catch {
            case ex: Exception =>
              log.error(ex, s"Error handling event ${event.eventType} with id ${event.eventId}")
              throw ex
          }
        }(scala.concurrent.ExecutionContext.global)
      }
      override def eventType: Class[T] = eventClass
    }
    
    eventBus.subscribe(listener, eventClass)
    log.info(s"Subscribed to event type: ${eventClass.getSimpleName}")
  }

  /**
   * Actor接收消息的处理模式
   * 子类应该组合使用 messageHandler 和 eventHandler
   */
  def receive: Receive = messageHandler.orElse(defaultHandler)

  /**
   * 处理传统Actor消息（子类可重写）
   */
  def messageHandler: Receive = PartialFunction.empty

  /**
   * 默认消息处理器
   */
  private def defaultHandler: Receive = {
    case msg =>
      log.warning(s"Unhandled message: $msg from ${sender()}")
  }

  /**
   * Actor启动时的初始化逻辑
   * 子类可以重写以设置事件订阅
   */
  override def preStart(): Unit = {
    super.preStart()
    setupEventSubscriptions()
    publishEvent(createActorStartedEvent())
    log.info(s"EventDrivenActor $actorId started")
  }

  /**
   * Actor停止时的清理逻辑
   */
  override def postStop(): Unit = {
    publishEvent(createActorStoppedEvent())
    cleanupEventSubscriptions()
    log.info(s"EventDrivenActor $actorId stopped")
    super.postStop()
  }

  /**
   * 设置事件订阅（子类重写）
   */
  protected def setupEventSubscriptions(): Unit = {
    // 子类实现具体的事件订阅逻辑
  }

  /**
   * 清理事件订阅（子类重写）
   */
  protected def cleanupEventSubscriptions(): Unit = {
    // 子类实现具体的清理逻辑
  }

  /**
   * 创建Actor启动事件（子类可重写）
   */
  protected def createActorStartedEvent(): DomainEvent = {
    SystemStarted(
      systemId = actorId,
      systemVersion = "1.0.0"
    )
  }

  /**
   * 创建Actor停止事件（子类可重写）
   */
  protected def createActorStoppedEvent(): DomainEvent = {
    SystemShutdown(
      systemId = actorId,
      reason = "Normal shutdown"
    )
  }
}

/**
 * 混合消息和事件处理的Actor
 * 提供更灵活的消息-事件桥接机制
 */
trait HybridMessageEventActor extends EventDrivenActor {

  /**
   * 消息到事件的转换器
   */
  protected def messageToEvent: PartialFunction[Message, DomainEvent] = PartialFunction.empty

  /**
   * 增强的消息处理器，自动发布相应的事件
   */
  override def messageHandler: Receive = {
    case msg: Message =>
      // 处理传统消息
      handleMessage(msg)
      
      // 尝试转换并发布事件
      messageToEvent.lift(msg).foreach(publishEventAsync)
      
    case other =>
      super.messageHandler(other)
  }

  /**
   * 处理传统消息的逻辑（子类实现）
   */
  protected def handleMessage(message: Message): Unit

  /**
   * 事件处理器（子类可实现）
   */
  protected def handleDomainEvent(event: DomainEvent): Unit = {
    log.debug(s"Received domain event: ${event.eventType} with id: ${event.eventId}")
  }
}

/**
 * 纯事件驱动Actor
 * 完全基于事件进行通信，不处理传统消息
 */
trait PureEventDrivenActor extends EventDrivenActor {

  /**
   * 纯事件驱动Actor不处理传统消息
   */
  override def messageHandler: Receive = {
    case msg =>
      log.warning(s"PureEventDrivenActor should not receive messages, got: $msg")
  }

  /**
   * 事件处理器（子类必须实现）
   */
  protected def handleDomainEvent(event: DomainEvent): Unit
}
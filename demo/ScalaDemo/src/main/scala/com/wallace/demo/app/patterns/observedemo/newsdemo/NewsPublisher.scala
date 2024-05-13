package com.wallace.demo.app.patterns.observedemo.newsdemo

import java.util
import scala.jdk.CollectionConverters.asScalaBufferConverter

/**
 * Author: biyu.huang
 * Date: 2024/4/8 17:23
 * Description:
 */
class NewsPublisher extends Subject {
  private final val observers: util.ArrayList[Observer] = new util.ArrayList[Observer]()

  override def addObserver(observer: Observer): Unit = {
    observers.add(observer)
  }

  override def removeObserver(observer: Observer): Unit = {
    observers.remove(observer)
  }

  override def notifyObservers(news: String): Unit = {
    if (observers.isEmpty) {
      logger.warn("no observers ...")
    } else {
      observers.asScala.foreach {
        o =>
          o.notify(news)
      }
    }
  }

  def addObservers(observers: Observer*): Unit = {
    observers.foreach {
      o =>
        addObserver(o)
    }
  }

  def publishNews(news: String): Unit = {
    logger.info("publish news -> %s".format(news))
    notifyObservers(news)
  }
}

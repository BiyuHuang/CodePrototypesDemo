package com.wallace.demo.app.prototype.observedemo.newsdemo

import com.typesafe.scalalogging.LazyLogging

import scala.reflect.runtime.universe._

/**
 * Author: biyu.huang
 * Date: 2024/4/8 17:21
 * Description: The subject which was observed
 */
trait Subject extends LazyLogging {
  def addObserver(observer: Observer): Unit

  def removeObserver(observer: Observer): Unit

  def notifyObservers(news: String): Unit
}

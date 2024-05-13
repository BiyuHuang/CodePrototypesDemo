package com.wallace.demo.app.patterns.observedemo

import scala.collection.mutable

/**
 * Author: biyu.huang
 * Date: 2023/1/30 16:51
 * Description: Observer Pattern(观察者模式)
 */
trait Observable {
  type Handler
  val callbacks = mutable.Map.empty[Handler, this.type => Unit]

  def observe(callback: this.type => Unit): Handler = {
    val handler: Handler = createHandle(callback)
    callbacks += (handler -> callback)
    handler
  }

  def unobserve(handle: Handler): Unit = {
    callbacks -= handle
  }

  def createHandle(callback: this.type => Unit): Handler

  def notifyListeners(): Unit = {
    for (callback <- callbacks.values) callback(this)
  }
}

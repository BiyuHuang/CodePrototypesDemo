package com.wallace.demo.app.patterns.observedemo

/**
 * Author: biyu.huang
 * Date: 2023/1/30 16:52
 * Description:
 */
trait DefaultHandlers extends Observable {
  override type Handler = this.type => Unit

  override def createHandle(callback: this.type => Unit): Handler = callback
}

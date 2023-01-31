package com.wallace.demo.app.prototype.observedemo

import com.wallace.demo.app.common.LogSupport

/**
 * Author: biyu.huang
 * Date: 2023/1/30 16:54
 * Description:
 */
object Boot extends LogSupport {
  def callback(index: Int): IntHolder => Unit = (x: IntHolder) => {
    logger.info(s"[observing $index] current value: ${x.get}")
    logger.info(x.toString)
  }

  def main(args: Array[String]): Unit = {
    val intValue: IntHolder = new IntHolder
    val callback1: IntHolder => Unit = callback(1000)
    val callback2: IntHolder => Unit = callback(2000)
    intValue.observe(callback1)
    intValue.observe(callback2)
    intValue.hold(10)

    intValue.unobserve(callback2)
    intValue.hold(12)
  }
}

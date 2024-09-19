package com.wallace.demo.app.queuedemo

/**
  * Created by 10192057 on 2018/6/4 0004.
  */
class BasicDoubleQueue extends DoubleQueue {
  override def get(): Double = buf.remove(0)

  override def put(x: Double): Unit = {
    buf += x
  }
}

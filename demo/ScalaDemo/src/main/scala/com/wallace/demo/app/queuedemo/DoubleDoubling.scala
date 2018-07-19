package com.wallace.demo.app.queuedemo

/**
  * Created by 10192057 on 2018/6/4 0004.
  */
trait DoubleDoubling extends DoubleQueue {
  abstract override def put(x: Double): Unit = super.put(x * 2.0)
}

trait DoubleIncrementing extends DoubleQueue {
  abstract override def put(x: Double): Unit = super.put(x + 1.0)
}

trait DoubleFiltering extends DoubleQueue {
  abstract override def put(x: Double): Unit = {
    if (x >= 0.0) super.put(x)
  }
}

package com.wallace.demo.app.queuedemo

/**
  * Created by 10192057 on 2018/6/4 0004.
  */
trait IntDoubling extends IntQueue {
  abstract override def put(x: Int): Unit = super.put(2 * x)
}

trait IntIncrementing extends IntQueue {
  abstract override def put(x: Int): Unit = super.put(x + 1)
}

trait IntFiltering extends IntQueue {
  abstract override def put(x: Int): Unit = {
    if (x >= 0) super.put(x)
  }
}

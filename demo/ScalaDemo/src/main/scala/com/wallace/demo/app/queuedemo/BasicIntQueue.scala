package com.wallace.demo.app.queuedemo

/**
  * Created by 10192057 on 2018/6/4 0004.
  */
class BasicIntQueue extends IntQueue {
  def size(): Int = buf.size

  def get(): Int = buf.remove(0)

  def put(x: Int): Unit = {
    buf += x
  }
}

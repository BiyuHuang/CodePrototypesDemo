package com.wallace.scalademo.multithread

import com.wallace.common.LogSupport

/**
  * Created by Wallace on 2016/10/12.
  * synchronized的用法
  */
class Thread_Test extends Runnable with LogSupport {
  override def run(): Unit = {
    this.synchronized {
      (0 to 5).foreach {
        x =>
          log.info(Thread.currentThread().getName + " synchronized loop " + x)
      }
    }
  }
}

package com.wallace.scala_demo.multiThread

/**
  * Created by Wallace on 2016/10/12.
  * synchronized的用法
  */
class Thread_Test extends Runnable {
  override def run(): Unit = {
    this.synchronized {
      (0 to 5).foreach {
        x =>
          System.out.println(Thread.currentThread().getName + " synchronized loop " + x)
      }
    }

//    (0 to 5).foreach {
//      x =>
//        System.out.println(Thread.currentThread().getName + " synchronized loop " + x)
//    }
  }
}

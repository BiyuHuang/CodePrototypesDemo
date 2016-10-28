package com.wallace.scala_demo.multiThread

/**
  * Created by Wallace on 2016/10/12.
  */
object MultiThreadDemo extends App {
  val t1 = new Thread_Test()
  val ta = new Thread(t1, "A")
  val tb = new Thread(t1, "B")

  ta.start()
  tb.start()
}

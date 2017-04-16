package com.wallace.demo.app.FactoryPatternDemo

import com.wallace.demo.app.common.LogSupport

/**
  * Created by Wallace on 2017/4/15.
  */
object FactoryDemoRunTest extends LogSupport {
  def main(args: Array[String]): Unit = {
    val a = 1.0
    val b = 2.0
    val operate = scala.io.StdIn.readLine("Please enter operation: ")
    println(OperationFactory.createOperate(operate, a, b).get)
  }
}

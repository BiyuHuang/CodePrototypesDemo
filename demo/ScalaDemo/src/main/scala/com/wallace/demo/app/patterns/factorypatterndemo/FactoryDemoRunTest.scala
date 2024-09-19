package com.wallace.demo.app.patterns.factorypatterndemo

import com.wallace.demo.app.common.LogSupport

/**
  * Created by Wallace on 2017/4/15.
  */
/**
 * Author: biyu.huang
 * Date: 2017/4/15 16:54
 * Description: Factory Pattern(工厂模式)
 */
object FactoryDemoRunTest extends LogSupport {
  def main(args: Array[String]): Unit = {
    val a = 1.0
    val b = 2.0
    val operate = scala.io.StdIn.readLine("Please enter operation: ")
    logger.info(s"${OperationFactory.createOperate(operate, a, b).getOrElse(0)}")
  }
}

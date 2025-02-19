package com.wallace.demo.app.unapply

import com.wallace.demo.app.common.LogSupport

/**
 * Author: biyu.huang
 * Date: 2025/2/19 10:54
 * Description:
 */
object DemoBoot extends LogSupport {
  def main(args: Array[String]): Unit = {
    val demo: DemoExpression = DemoExpression("Wallace", 33)
    demo match {
      case DemoExpression(name, age) => logger.info(s"Name: $name, Age: $age")
      case _ => logger.info("No match.")
    }
  }
}

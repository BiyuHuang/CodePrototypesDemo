package com.wallace.demo.app.unapply

/**
 * Author: biyu.huang
 * Date: 2025/2/19 10:21
 * Description:
 */
case class DemoExpression(name: String, age: Int)

object DemoExpression {
  def unapply(arg: DemoExpression): Option[(String, Int)] = {
    Some((arg.name, arg.age))
  }
}

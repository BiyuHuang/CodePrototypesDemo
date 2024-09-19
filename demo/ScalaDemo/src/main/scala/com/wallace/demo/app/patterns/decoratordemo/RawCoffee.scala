package com.wallace.demo.app.patterns.decoratordemo

/**
 * Author: biyu.huang
 * Date: 2024/5/13 11:58
 * Description:
 */
class RawCoffee(price: Double = 10.0) extends Coffee {
  override def cost(): Double = price

  override def getDescription: String = "Raw Coffee"
}

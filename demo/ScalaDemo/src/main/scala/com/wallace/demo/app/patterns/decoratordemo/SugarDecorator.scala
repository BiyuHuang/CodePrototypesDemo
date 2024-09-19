package com.wallace.demo.app.patterns.decoratordemo

/**
 * Author: biyu.huang
 * Date: 2024/5/13 12:07
 * Description:
 */
class SugarDecorator(coffee: Coffee, price: Double = 0.5) extends CoffeeDecorator(coffee) {
  override def cost(): Double = coffee.cost() + price

  override def getDescription: String = {
    Array(coffee.getDescription, "Sugar").mkString(" + ")
  }
}

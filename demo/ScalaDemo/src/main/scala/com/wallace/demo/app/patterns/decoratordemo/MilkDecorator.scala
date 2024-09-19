package com.wallace.demo.app.patterns.decoratordemo

/**
 * Author: biyu.huang
 * Date: 2024/5/13 12:05
 * Description:
 */
class MilkDecorator(coffee: Coffee, price: Double = 2.0) extends CoffeeDecorator(coffee) {
  override def cost(): Double = coffee.cost() + price

  override def getDescription: String = {
    Array(coffee.getDescription, "Milk").mkString(" + ")
  }
}

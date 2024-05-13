package com.wallace.demo.app.patterns.decoratordemo

import com.typesafe.scalalogging.LazyLogging

/**
 * Author: biyu.huang
 * Date: 2024/5/13 12:09
 * Description: Decorator Pattern(装饰者模式)
 */
object DecoratorDemo extends LazyLogging {
  private val logFormat: String = "Cost -> %s, Description -> %s"
  private val coffeePrice: Double = 10.1
  private val milkPrice: Double = 2.2
  private val sugarPrice: Double = 0.6

  def main(args: Array[String]): Unit = {
    val coffee: RawCoffee = new RawCoffee(coffeePrice)
    val milkCoffee: MilkDecorator = new MilkDecorator(coffee, milkPrice)
    val sweetCoffee: SugarDecorator = new SugarDecorator(coffee, sugarPrice)
    val sweetMilkCoffee: SugarDecorator = new SugarDecorator(milkCoffee, sugarPrice)
    logger.info(logFormat.format(coffee.cost(), coffee.getDescription))
    logger.info(logFormat.format(milkCoffee.cost(), milkCoffee.getDescription))
    logger.info(logFormat.format(sweetCoffee.cost(), sweetCoffee.getDescription))
    logger.info(logFormat.format(sweetMilkCoffee.cost(), sweetMilkCoffee.getDescription))
  }
}

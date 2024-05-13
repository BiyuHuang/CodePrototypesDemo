package com.wallace.demo.app.patterns.templatemethoddemo

import com.typesafe.scalalogging.LazyLogging

/**
 * Author: biyu.huang
 * Date: 2024/5/13 15:04
 * Description:
 */
trait Beverage extends LazyLogging {
  def prepareBeverage(): Unit = {
    boilWater()
    brew()
    pourToCup()
    addCondiments()
    logger.info("5 -> here you are!")
  }

  private def boilWater(): Unit = {
    logger.info("1 -> 烧水")
  }

  def brew(): Unit

  def pourToCup(): Unit = {
    logger.info("3 -> 倒入杯中")
  }

  def addCondiments(): Unit
}

package com.wallace.demo.app.patterns.templatemethoddemo

/**
 * Author: biyu.huang
 * Date: 2024/5/13 15:08
 * Description:
 */
class Coffee extends Beverage {
  override def brew(): Unit = {
    logger.info("2 -> 冲泡咖啡")
  }

  override def addCondiments(): Unit = {
    logger.info("4 -> 加糖和牛奶")
  }
}

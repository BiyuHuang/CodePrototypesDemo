package com.wallace.demo.app.patterns.templatemethoddemo

/**
 * Author: biyu.huang
 * Date: 2024/5/13 15:12
 * Description:
 */
class Tea extends Beverage {
  override def brew(): Unit = {
    logger.info("2 -> 冲泡茶叶")
  }

  override def addCondiments(): Unit = {
    logger.info("4 -> 加柠檬")
  }
}

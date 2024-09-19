package com.wallace.demo.app.patterns.templatemethoddemo

/**
 * Author: biyu.huang
 * Date: 2024/5/13 15:13
 * Description: Template Method Pattern(模板方法模式)
 */
object TemplateMethodDemo {
  def main(args: Array[String]): Unit = {
    val coffee = new Coffee()
    coffee.prepareBeverage()

    val tea =new Tea()
    tea.prepareBeverage()
  }
}

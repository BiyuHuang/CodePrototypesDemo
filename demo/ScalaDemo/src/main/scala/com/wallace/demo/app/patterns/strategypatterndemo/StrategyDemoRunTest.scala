package com.wallace.demo.app.patterns.strategypatterndemo

import com.wallace.demo.app.common.LogSupport

/**
 * Author: biyu.huang
 * Date: 2017/4/16 12:09
 * Description: Strategy Pattern(策略模式)
 */
object StrategyDemoRunTest extends LogSupport {
  def main(args: Array[String]): Unit = {
    val mode: String = scala.io.StdIn.readLine("Please select a mode: ")
    val cashStrategy: StrategyContext = mode match {
      case "Normal" =>
        new StrategyContext(new CashNormal)
      case "RebateWithReturn" =>
        new StrategyContext(new CashReturn("300", "100"))
      case "RebateWithOutReturn" =>
        new StrategyContext(new CashRebate("0.8"))
    }
    val money: Double = scala.io.StdIn.readLine("Please input total amount: ").toDouble
    val res: Double = cashStrategy.acceptCash(money)
    logger.info("Current mode -> %s, Total amount -> %s RMB".format(cashStrategy.modeName, res))
  }
}

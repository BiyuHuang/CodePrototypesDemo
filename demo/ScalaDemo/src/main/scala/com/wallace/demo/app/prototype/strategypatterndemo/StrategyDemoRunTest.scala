package com.wallace.demo.app.prototype.strategypatterndemo

import com.wallace.demo.app.common.LogSupport

/**
  * Created by Wallace on 2017/4/16.
  */
object StrategyDemoRunTest extends LogSupport {
  def main(args: Array[String]): Unit = {
    val m_Type: String = scala.io.StdIn.readLine("Please input an mode: ")
    val cashStrategy: StrategyContext = m_Type match {
      case "Normal" =>
        new StrategyContext(new CashNormal)
      case "RebateWithReturn" =>
        new StrategyContext(new CashReturn("300", "100"))
      case "RebateWithOutReturn" =>
        new StrategyContext(new CashRebate("0.8"))
    }
    val money: Double = scala.io.StdIn.readLine("Please input total money: ").toDouble
    val res: Double = cashStrategy.getAcceptCashResult(money)
    logger.info(s"Total money: $res RMB")
  }
}

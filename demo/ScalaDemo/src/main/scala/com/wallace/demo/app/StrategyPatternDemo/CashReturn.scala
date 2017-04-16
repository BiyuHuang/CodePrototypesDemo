package com.wallace.demo.app.StrategyPatternDemo

import scala.util.Try

/**
  * Created by Wallace on 2017/4/16.
  */
class CashReturn(m_Condition: String, m_Return: String) extends CashSuper {
  private val moneyCondition: Double = Try(m_Condition.toDouble).getOrElse(0.0)
  private val moneyReturn: Double = Try(m_Return.toDouble).getOrElse(0.0)

  override def acceptCash(money: Double): Double = {
    money match {
      case v if v >= moneyCondition && moneyReturn > 0 =>
        money - Math.floor(money / moneyCondition) * moneyReturn
      case _ =>
        money
    }
  }

  override def algTest: String = ???
}

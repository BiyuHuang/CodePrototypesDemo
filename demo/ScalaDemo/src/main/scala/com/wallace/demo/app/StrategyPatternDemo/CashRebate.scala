package com.wallace.demo.app.StrategyPatternDemo

import scala.util.Try

/**
  * Created by Wallace on 2017/4/16.
  */
class CashRebate(m_Rebate: String) extends CashSuper {
  private val moneyRebate: Double = Try(m_Rebate.toDouble).getOrElse(1.0)

  override def acceptCash(money: Double): Double = {
    moneyRebate * money
  }

  override def algTest: String = ???
}

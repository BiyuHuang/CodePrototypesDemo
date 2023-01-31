package com.wallace.demo.app.prototype.strategypatterndemo

import scala.util.Try

/**
  * Created by Wallace on 2017/4/16.
  */
class CashRebate(m_Rebate: String) extends CashSuper {
  private val DEFAULT_REBATE = 1.0
  private val moneyRebate: Double = Try(m_Rebate.toDouble).getOrElse(DEFAULT_REBATE)

  override def acceptCash(money: Double): Double = {
    moneyRebate * money
  }

  override def algTest: String = ???
}

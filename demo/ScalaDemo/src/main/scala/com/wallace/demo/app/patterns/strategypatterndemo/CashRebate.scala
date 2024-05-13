package com.wallace.demo.app.patterns.strategypatterndemo

import scala.util.Try

/**
  * Created by Wallace on 2017/4/16.
  */
class CashRebate(rebate: String) extends CashSuper {
  private val DEFAULT_REBATE = 1.0
  private val moneyRebate: Double = Try(rebate.toDouble).getOrElse(DEFAULT_REBATE)

  override def acceptCash(money: Double): Double = {
    moneyRebate * money
  }

  override def modeName: String = "Cash Rebate"
}

package com.wallace.demo.app.patterns.strategypatterndemo

import scala.util.Try

/**
  * Created by Wallace on 2017/4/16.
  */
class CashReturn(condition: String, cashBack: String) extends CashSuper {
  private val cashCondition: Double = Try(condition.toDouble).getOrElse(0.0)
  private val cashReturn: Double = Try(cashBack.toDouble).getOrElse(0.0)

  override def acceptCash(money: Double): Double = {
    money match {
      case v if v >= cashCondition && cashReturn > 0 =>
        money - Math.floor(money / cashCondition) * cashReturn
      case _ =>
        money
    }
  }

  override def modeName: String = "Cash Back"
}

package com.wallace.demo.app.prototype.strategypatterndemo

/**
  * Created by Wallace on 2017/4/16.
  */
class StrategyContext(cs: CashSuper) {
  private val strategy: CashSuper = cs

  def getAcceptCashResult(money: Double): Double = {
    strategy.acceptCash(money)
  }

  def getAlgTest: String = {
    strategy.algTest
  }
}

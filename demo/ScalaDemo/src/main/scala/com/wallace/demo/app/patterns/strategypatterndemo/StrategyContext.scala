package com.wallace.demo.app.patterns.strategypatterndemo

/**
 * Created by Wallace on 2017/4/16.
 */
class StrategyContext(cs: CashSuper) extends CashSuper {
  private val strategy: CashSuper = cs

  //  def getAcceptCashResult(money: Double): Double = {
  //    strategy.acceptCash(money)
  //  }
  //
  //  def getModeName: String = {
  //    strategy.modeName
  //  }

  override def acceptCash(money: Double): Double = {
    strategy.acceptCash(money)
  }

  override def modeName: String = {
    strategy.modeName
  }
}

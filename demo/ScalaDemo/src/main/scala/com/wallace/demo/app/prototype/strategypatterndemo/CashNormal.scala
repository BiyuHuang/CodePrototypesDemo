package com.wallace.demo.app.prototype.strategypatterndemo

/**
  * Created by Wallace on 2017/4/16.
  */
class CashNormal extends CashSuper {
  override def acceptCash(money: Double): Double = money

  override def algTest: String = ???
}

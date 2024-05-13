package com.wallace.demo.app.patterns.strategypatterndemo

/**
  * Created by Wallace on 2017/4/16.
  */
abstract class CashSuper {
  def acceptCash(money: Double): Double

  def modeName: String
}

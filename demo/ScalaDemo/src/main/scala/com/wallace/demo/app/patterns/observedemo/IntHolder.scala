package com.wallace.demo.app.patterns.observedemo

/**
 * Author: biyu.huang
 * Date: 2023/1/30 16:53
 * Description:
 */
class IntHolder extends Observable with DefaultHandlers {
  private var value: Int = Int.MinValue

  def get: Int = value

  def hold(newValue: Int): Unit = {
    value = newValue
    notifyListeners()
  }

  override def toString: String = "IntStore(" + value + ")"
}

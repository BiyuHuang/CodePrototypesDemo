package com.wallace.demo.app.prototype.factorypatterndemo

/**
  * Created by Wallace on 2017/4/15.
  */
class OperationSub(numA: Double, numB: Double) extends Operation[Double] {
  override val numberA: Double = numA
  override val numberB: Double = numB

  override def calcResult: Option[Double] = {
    if (numberA > numberB) {
      Some(numberA - numberB)
    } else {
      Some(numberB - numberA)
    }
  }
}

object OperationSub {
  def apply(numA: Double, numB: Double): OperationSub = new OperationSub(numA, numB)
}

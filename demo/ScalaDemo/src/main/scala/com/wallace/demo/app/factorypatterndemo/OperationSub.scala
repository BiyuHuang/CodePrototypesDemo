package com.wallace.demo.app.factorypatterndemo

import scala.util.Try

/**
  * Created by Wallace on 2017/4/15.
  */
class OperationSub(numA: Double, numB: Double) extends Operation[Double] {
  override val numberA: Double = numA
  override val numberB: Double = numB

  override def calcResult: Option[Double] = {
    if (numberA > numberB) {
      Try(numberA - numberB).toOption
    } else {
      Try(numberB - numberA).toOption
    }
  }
}

object OperationSub {
  def apply(numA: Double, numB: Double): OperationSub = new OperationSub(numA, numB)
}

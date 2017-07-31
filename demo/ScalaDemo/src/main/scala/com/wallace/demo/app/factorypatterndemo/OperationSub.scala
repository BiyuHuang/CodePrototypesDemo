package com.wallace.demo.app.factorypatterndemo

import scala.util.Try

/**
  * Created by Wallace on 2017/4/15.
  */
class OperationSub(A: Double, B: Double) extends Operation[Double] {
  override val numberA: Double = A
  override val numberB: Double = B

  override def calcResult: Option[Double] = {
    if (numberA > numberB) {
      Try(numberA - numberB).toOption
    } else {
      Try(numberB - numberA).toOption
    }
  }
}

object OperationSub {
  def apply(A: Double, B: Double): OperationSub = new OperationSub(A, B)
}

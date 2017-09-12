package com.wallace.demo.app.factorypatterndemo

import scala.util.Try

/**
  * Created by Wallace on 2017/4/15.
  */
class OperationMul(numA: Double, numB: Double) extends Operation[Double] {
  override val numberA: Double = numA
  override val numberB: Double = numB

  override def calcResult: Option[Double] = {
    Try(numberA * numberB).toOption
  }
}

object OperationMul {
  def apply(numA: Double, numB: Double): OperationMul = new OperationMul(numA, numB)
}

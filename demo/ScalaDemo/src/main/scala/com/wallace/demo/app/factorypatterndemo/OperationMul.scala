package com.wallace.demo.app.factorypatterndemo

import scala.util.Try

/**
  * Created by Wallace on 2017/4/15.
  */
class OperationMul(A: Double, B: Double) extends Operation[Double] {
  override val numberA: Double = A
  override val numberB: Double = B

  override def calcResult: Option[Double] = {
    Try(numberA * numberB).toOption
  }
}

object OperationMul {
  def apply(A: Double, B: Double): OperationMul = new OperationMul(A, B)
}

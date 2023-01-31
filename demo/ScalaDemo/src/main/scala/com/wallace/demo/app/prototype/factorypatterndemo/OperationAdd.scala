package com.wallace.demo.app.prototype.factorypatterndemo

import scala.util.Try

/**
  * Created by Wallace on 2017/4/15.
  */
class OperationAdd(numA: Double, numB: Double) extends Operation[Double] {
  override val numberA: Double = numA
  override val numberB: Double = numB

  override def calcResult: Option[Double] = {
    Try(numberA + numberB).toOption
  }
}

object OperationAdd {
  def apply(numA: Double, numB: Double): OperationAdd = new OperationAdd(numA, numB)
}

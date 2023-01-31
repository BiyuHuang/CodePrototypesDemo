package com.wallace.demo.app.prototype.factorypatterndemo

import com.wallace.demo.app.common.LogSupport

import scala.util.Try

/**
  * Created by Wallace on 2017/4/15.
  */
class OperationDiv(numA: Double, numB: Double) extends Operation[Double] with LogSupport {
  override val numberA: Double = numA
  override val numberB: Double = numB

  override def calcResult: Option[Double] = {
    if (numberA == 0 && numberB == 0) {
      logger.info("至少有一个数不为0.")
    }

    if (numberB != 0) Try(numberA / numberB).toOption else Try(numberB / numberA).toOption
  }
}

object OperationDiv {
  def apply(numA: Double, numB: Double): OperationDiv = new OperationDiv(numA, numB)
}

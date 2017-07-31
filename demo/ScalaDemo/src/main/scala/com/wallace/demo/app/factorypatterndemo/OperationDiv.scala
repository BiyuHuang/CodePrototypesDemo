package com.wallace.demo.app.factorypatterndemo

import com.wallace.demo.app.common.LogSupport

import scala.util.Try

/**
  * Created by Wallace on 2017/4/15.
  */
class OperationDiv(A: Double, B: Double) extends Operation[Double] with LogSupport {
  override val numberA: Double = A
  override val numberB: Double = B

  override def calcResult: Option[Double] = {
    if (numberA == 0 && numberB == 0) {
      log.info("至少有一个数不为0.")
    }

    if (numberB != 0) Try(numberA / numberB).toOption else Try(numberB / numberA).toOption
  }
}

object OperationDiv {
  def apply(A: Double, B: Double): OperationDiv = new OperationDiv(A, B)
}

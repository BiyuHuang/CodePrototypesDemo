package com.wallace.demo.app.patterns.factorypatterndemo

/**
 * Created by Wallace on 2017/4/15.
 */
object OperationFactory {
  def createOperate(opSymbol: String, num_A: Double, num_B: Double): Option[Double] = {
    opSymbol match {
      case "+" =>
        OperationAdd(num_A, num_B).calcResult
      case "-" =>
        OperationSub(num_A, num_B).calcResult
      case "*" =>
        OperationMul(num_A, num_B).calcResult
      case "/" =>
        OperationDiv(num_A, num_B).calcResult
      case _ =>
        None
    }
  }
}

package com.wallace.demo.app.FactoryPatternDemo

/**
  * Created by Wallace on 2017/4/15.
  */
object OperationFactory {
  def createOperate(opSymbol: String, num_A: Double, num_B: Double): Option[Double] = {
    var result: Option[Double] = null
    opSymbol match {
      case "+" =>
        result = OperationAdd(num_A, num_B).calcResult
      case "-" =>
        result = OperationSub(num_A, num_B).calcResult
      case "*" =>
        result = OperationMul(num_A, num_B).calcResult
      case "/" =>
        result = OperationDiv(num_A, num_B).calcResult
      case _ =>
        result = null
    }
    result
  }
}

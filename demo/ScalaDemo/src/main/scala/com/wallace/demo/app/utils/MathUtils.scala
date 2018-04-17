package com.wallace.demo.app.utils

/**
  * Created by 10192057 on 2018/4/17 0017.
  */
object MathUtils {
  def parseInt(in: String, radio: String): Int = {
    radio match {
      case "d" => parseInt(in, 10)
      case "h" => parseInt(in, 16)
      case "o" => parseInt(in, 8)
      case "b" => parseInt(in, 2)
      case _ => parseInt(in, 10)
    }
  }

  def parseInt(in: String, radio: Int): Int = Integer.parseInt(in, radio)

  def formatValue(in: Int, radio: String): String = {
    radio match {
      case "d" => Integer.toString(in)
      case "h" => Integer.toHexString(in)
      case "o" => Integer.toOctalString(in)
      case "b" => Integer.toBinaryString(in)
      case _ => in.toString
    }
  }

  def formatValue(in: Double, radio: String): String = formatValue(in.toInt, radio)

  def execOperations(value: Int, operator: String, operand: Int): Int = {
    operator match {
      case "+" => value + operand
      case "-" => value - operand
      case "*" => if (operand != 0) value * operand else value
      case "/" => if (operand != 0) value / operand else value
      case "%" => if (operand != 0) value % operand else value
    }
  }

  def execOperations(value: Double, operator: String, operand: Double): Double = {
    operator match {
      case "+" => value + operand
      case "-" => value - operand
      case "*" => if (operand != 0) value * operand else value
      case "/" => if (operand != 0) value / operand else value
      case "%" => if (operand != 0) value % operand else value
    }
  }
}

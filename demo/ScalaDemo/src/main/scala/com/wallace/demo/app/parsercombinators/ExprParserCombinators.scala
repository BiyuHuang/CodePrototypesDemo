package com.wallace.demo.app.parsercombinators

import scala.util.parsing.combinator._

/**
  * Created by 10192057 on 2018/4/9 0009.
  */
class ExprParserCombinators extends JavaTokenParsers {
  private def expr: Parser[Double] = {
    term ~ (("+" | "-") ~ term).* ^^ {
      case t ~ list =>
        var result = t
        list.foreach {
          case "+" ~ t0 => result += t0
          case "-" ~ t0 => result -= t0
        }
        result
    }
  }

  private def term: Parser[Double] = {
    factor ~ (("*" | "/" | "%") ~ factor).* ^^ {
      case f ~ list =>
        var result = f
        list.foreach {
          case "*" ~ f0 => result *= f0
          case "/" ~ f0 => result /= f0
          case "%" ~ f0 => result %= f0
        }
        result
    }
  }


  private def factor: Parser[Double] = floatingPointNumber ^^ (_.toDouble) | "(" ~> expr <~ ")"

  /**
    * This is the parse method
    *
    * @param source a String source to be parsed
    * @return Double type result
    */
  def parse(source: String): Double = parseAll(expr, source).get
}

object ExprParserCombinators {
  def main(args: Array[String]): Unit = {
    val parser: ExprParserCombinators = new ExprParserCombinators
    assert(parser.parse("1 + 7 - 3") == 5.0, "Failed to parse input source.")
    assert(parser.parse("1 + 2 * 7 + 3 / 4") == 15.75, "Failed to parse input source.")
    assert(parser.parse("1 + 2 * (7 + 3) / 4") == 6.0, "Failed to parse input source.")
    assert(parser.parse("1 + 2 / 0") == Double.PositiveInfinity, "Failed to parse input source.")
    assert(parser.parse("3 % 4 + 10 / 5") == 5.0, "Failed to parse input source.")
  }
}

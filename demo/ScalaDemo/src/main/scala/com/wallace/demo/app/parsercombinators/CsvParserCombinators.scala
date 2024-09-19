package com.wallace.demo.app.parsercombinators

import scala.language.postfixOps
import scala.util.matching.Regex
import scala.util.parsing.combinator._

/**
  * Created by 10192057 on 2018/4/9 0009.
  */
class CsvParserCombinators extends RegexParsers {
  override val skipWhitespace: Boolean = false

  override protected val whiteSpace: Regex = """[ \t]""".r

  def comma: String = ","

  def dQuote: String = "\""

  def dQuote2: Parser[String] = "\"\"" ^^ (_ => "\"")

  def cr: String = "\r"

  def lf: String = "\n"

  def crlf: String = "\r\n"

  def txt: Regex = "[^\",\r\n]".r

  def record: Parser[List[String]] = rep1sep(field, comma)

  def field: Parser[String] = escaped | nonEscaped

  def escaped: Parser[String] = (dQuote ~> ((txt | comma | cr | lf | dQuote2) *) <~ dQuote) ^^ (ls => ls.mkString("").trim)

  def nonEscaped: Parser[String] = (txt *) ^^ (ls => ls.mkString("").trim)

  def file: Parser[List[List[String]]] = repsep(record, crlf) <~ opt(crlf)

  def parse(s: String): List[List[String]] = parseAll(file, s) match {
    case Success(res, _) => res
    case _ => List[List[String]]()
  }
}

object CsvParserCombinators {
  def main(args: Array[String]): Unit = {
    val parser = new CsvParserCombinators
    val res: List[List[String]] = parser.parse("\"John,Doe\",\"123 Main St\",\"Brown Eyes\"")
    val res1: List[List[String]] = parser.parse(""""foo","bar", 123""" + "\r\n" + "hello, world, 456" + "\r\n" + """ spam, 789, egg""")
    res.foreach {
      elem =>
        elem.foreach(println)
    }

    res1.foreach {
      elem =>
        elem.foreach(println)
    }
    assert(res.head.size == 3, "Failed to parse csv text")
  }
}

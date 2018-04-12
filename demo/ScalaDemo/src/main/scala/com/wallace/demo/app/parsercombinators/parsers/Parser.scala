package com.wallace.demo.app.parsercombinators.parsers

/**
  * Created by 10192057 on 2018/4/11 0011.
  */
trait Parser {
  /**
    * Any initialization / startup needed by the Parser.
    */
  def initialize(): Unit

  /**
    * Parsed of a single {@link numeric record}.
    *
    * @param record numeric field to be parsed
    * @return single numeric field, or { @code null} if the numeric field
    *         is to be dropped (i.e. filtered out).
    */
  def parse(record: Array[String]): String

  //  trait Builder {
  //    def build: Parser
  //  }

}

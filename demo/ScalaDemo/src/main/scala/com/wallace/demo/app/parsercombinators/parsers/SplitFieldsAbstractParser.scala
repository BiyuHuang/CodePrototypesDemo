package com.wallace.demo.app.parsercombinators.parsers

/**
  * Created by 10192057 on 2018/4/12 0012.
  */
class SplitFieldsAbstractParser extends AbstractParser {
  /**
    * Any initialization / startup needed by the Parser.
    */
  override def initialize(): Unit = ???

  /**
    * Parsed of a single {@link numeric record}.
    *
    * @param record numeric field to be parsed
    * @return single numeric field, or { @code null} if the numeric field
    *         is to be dropped (i.e. filtered out).
    */
  override def parse(record: Array[String]): String = ???

  override def configure(context: MethodContext): Unit = ???
}

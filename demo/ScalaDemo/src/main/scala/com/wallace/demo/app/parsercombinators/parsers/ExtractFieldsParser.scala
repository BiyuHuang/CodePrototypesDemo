package com.wallace.demo.app.parsercombinators.parsers

/**
  * Created by 10192057 on 2018/4/11 0011.
  */
class ExtractFieldsParser extends Parser {

  override def initialize(): Unit = {

  }

  override def parse(record: Array[String]): Array[String] = ???

  object Builder extends super.Builder {
    override def build: Parser = {

    }
  }
}

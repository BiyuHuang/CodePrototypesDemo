package com.wallace.demo.app.parsercombinators.parsers

import scala.collection.mutable.ArrayBuffer

/**
  * Created by 10192057 on 2018/4/11 0011.
  */
class ParserChain(parsers: Array[Parser]) extends Parser {
  //  private val parsers: mutable.Map[String, Parser] = mutable.Map.empty
  //  private val parsersLs: mutable.HashMap[String, Parser] = mutable.HashMap.empty[String, Parser]
  //  private val parsers: ArrayBuffer[Parser] = new ArrayBuffer[Parser]()

  override def initialize(): Unit = {
    parsers.foreach {
      parser =>
        parser.initialize()
    }
  }

  override def parse(record: Array[String]): Array[String] = {
    //TODO 接口需重新定义
    var res: Array[String] = Array.empty
    parsers.foreach {
      parser =>
        res = parser.parse(record)
    }
    res
  }
}

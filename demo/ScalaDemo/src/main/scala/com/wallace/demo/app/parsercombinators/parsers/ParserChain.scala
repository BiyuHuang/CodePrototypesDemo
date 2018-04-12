package com.wallace.demo.app.parsercombinators.parsers

import scala.collection.mutable.ArrayBuffer

/**
  * Created by 10192057 on 2018/4/11 0011.
  */
class ParserChain(parsers: Map[String, Parser]) extends Parser {
  //  private val parsers: mutable.Map[String, Parser] = mutable.Map.empty
  //  private val parsersLs: mutable.HashMap[String, Parser] = mutable.HashMap.empty[String, Parser]
  //  private val parsers: ArrayBuffer[Parser] = new ArrayBuffer[Parser]()

  override def initialize(): Unit = {
    parsers.foreach {
      parser =>
        parser._2.initialize()
    }
  }

  override def parse(record: Array[String]): String = {
    //TODO 接口需重新定义
    val res = new StringBuilder

    record.indices.foreach {
      i =>
        parsers.foreach {
          parser =>
            res.append(parser._2.parse(record))
        }
        res.append(",")
    }
    res.result()
  }

  override def configure(context: Context): Unit = {

  }
}

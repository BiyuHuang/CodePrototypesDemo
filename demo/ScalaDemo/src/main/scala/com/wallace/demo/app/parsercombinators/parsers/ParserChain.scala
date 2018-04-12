package com.wallace.demo.app.parsercombinators.parsers

import java.util
import scala.collection.JavaConverters._

/**
  * Created by 10192057 on 2018/4/11 0011.
  */

case class RawDataMetaData(fieldsSep: String,
                           m_SrcColumnsFields: util.HashMap[String, Int],
                           m_TgtColumnsFields: Array[FieldInfo])

case class FieldInfo(name: String, index: Int, methodKey: String)

class ParserChain(rawDataMetaData: RawDataMetaData, parsers: util.HashMap[String, AbstractParser]) extends AbstractParser {
  private val m_TgtColumnsFields: Array[FieldInfo] = rawDataMetaData.m_TgtColumnsFields
  private val symbol: Int = m_TgtColumnsFields.length - 1

  override def initialize(): Unit = {
    parsers.asScala.foreach {
      parser =>
        parser._2.initialize()
    }
  }

  override def parse(record: Array[String]): String = {
    //TODO 接口需重新定义
    val res: StringBuilder = new StringBuilder
    m_TgtColumnsFields.indices.foreach {
      i =>
        val field = m_TgtColumnsFields(i)
        val value = parsers.get(field.methodKey).parse(record)
        if (i < symbol) res.append(value).append(rawDataMetaData.fieldsSep) else res.append(value)
    }
    res.toString()
  }

  override def configure(context: MethodContext): Unit = {
    //no-op
  }
}

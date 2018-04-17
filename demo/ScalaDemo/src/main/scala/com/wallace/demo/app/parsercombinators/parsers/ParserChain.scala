package com.wallace.demo.app.parsercombinators.parsers

import java.util

import com.wallace.demo.app.utils.FuncUtil

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

  def initialize(): Unit = {
    initialize(rawDataMetaData.m_SrcColumnsFields)
    parsers.asScala.foreach {
      parser =>
        parser._2.initialize(rawDataMetaData.m_SrcColumnsFields)
    }
  }

  def parse(recordLine: String): String = {
    val record: Array[String] = if (recordLine.contains("\"")) {
      FuncUtil.split(recordLine, rawDataMetaData.fieldsSep, "\"")
    } else if (recordLine.contains("\'")) {
      FuncUtil.split(recordLine, rawDataMetaData.fieldsSep, "\'")
    } else {
      recordLine.split(rawDataMetaData.fieldsSep, -1)
    }
    parse(record)
  }

  override def parse(record: Array[String], fieldInfo: FieldInfo = FieldInfo("", -1, MethodKeyType.default)): String = {
    val res: StringBuilder = new StringBuilder
    assert(m_SrcColumnsFields.size() == record.length, "ArrayIndexOutOfBoundsException")
    m_TgtColumnsFields.indices.foreach {
      i =>
        val field: FieldInfo = m_TgtColumnsFields(i)
        val value = parsers.get(field.methodKey).parse(record, field)
        if (i < symbol) res.append(value).append(rawDataMetaData.fieldsSep) else res.append(value)
    }
    res.toString()
  }

  override def configure(context: MethodContext): Unit = {
    //no-op
  }
}

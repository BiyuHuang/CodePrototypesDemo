package com.wallace.demo.app.parsercombinators.parsers

import scala.util.Try

/**
  * Created by 10192057 on 2018/4/13 0013.
  */
class SubStringFieldsParser extends AbstractParser {

  private var startIndex: Int = -1
  private var endIndex: Int = -1

  /**
    * Parsed of a single line of Record.
    *
    * @param record numeric fields of Record to be parsed
    * @return single numeric field
    */
  override def parse(record: Array[String], field: FieldInfo): String = {
    if (m_SrcColumnsFields.containsKey(field.name)) {
      val value: String = record(m_SrcColumnsFields.get(field.name))
      val len: Int = value.length
      if (len == 0) {
        ""
      } else {
        val start = if (startIndex < 0) len + startIndex else startIndex
        val end = if (endIndex < 0) len + endIndex else if (endIndex == 0) len else endIndex
        Try(value.substring(start, end)).getOrElse("")
      }
    } else {
      ""
    }
  }

  /**
    * @param context MethodContext
    **/
  override def configure(context: MethodContext): Unit = {
    startIndex = context.methodMetaData.conf.getOrElse("startindex", "0").toInt
    endIndex = context.methodMetaData.conf.getOrElse("endindex", "0").toInt
  }
}

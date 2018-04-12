package com.wallace.demo.app.parsercombinators.parsers

import java.util

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * Created by 10192057 on 2018/4/11 0011.
  */
class ExtractFieldsAbstractParser extends AbstractParser {
  private val m_SrcColumnsFields: util.HashMap[String, Int] = new util.HashMap[String, Int]

  override def initialize(): Unit = {
    // no-op
  }

  override def parse(record: Array[String], fieldInfo: FieldInfo): String = {
    record(m_SrcColumnsFields.get(fieldInfo.name))
  }

  override def configure(context: MethodContext, srcColumnsFields: util.HashMap[String, Int]): Unit = {
    m_SrcColumnsFields.putAll(srcColumnsFields)
  }
}

package com.wallace.demo.app.parsercombinators.parsers

import java.util

/**
  * Created by 10192057 on 2018/4/12 0012.
  */
class SplitFieldsAbstractParser extends AbstractParser {
  private val m_SrcColumnsFields: util.HashMap[String, Int] = new util.HashMap[String, Int]

  /**
    * Any initialization / startup needed by the Parser.
    */
  override def initialize(): Unit = {
    //no-op
  }

  override def parse(record: Array[String], fieldInfo: FieldInfo): String = {
    ""
  }

  override def configure(context: MethodContext, srcColumnsFields: util.HashMap[String, Int]): Unit = {
    m_SrcColumnsFields.putAll(srcColumnsFields)
  }
}

package com.wallace.demo.app.parsercombinators.parsers

import java.util

import com.wallace.demo.app.common.{LogSupport, MethodMetaData}

/**
  * Created by 10192057 on 2018/4/11 0011.
  */
trait AbstractParser extends Configurable {
  protected val m_SrcColumnsFields: util.HashMap[String, Int] = new util.HashMap[String, Int]

  /**
    * Necessary Initialization / startup needed by the Parser.
    *
    * @param srcColumnsFields Raw data header
    */
  def initialize(srcColumnsFields: util.HashMap[String, Int]): Unit = synchronized {
    m_SrcColumnsFields.putAll(srcColumnsFields)
  }

  /**
    * Parsed of a single line of Record.
    *
    * @param record numeric fields of Record to be parsed
    * @return single numeric field
    */
  def parse(record: Array[String], field: FieldInfo): String

}

trait Configurable extends LogSupport {
  /**
    * @param context MethodContext
    **/
  def configure(context: MethodContext): Unit
}

case class MethodContext(methodKey: String, methodMetaData: MethodMetaData)
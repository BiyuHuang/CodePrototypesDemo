package com.wallace.demo.app.parsercombinators.parsers

import java.util.concurrent.ConcurrentHashMap

import com.wallace.demo.app.common.{LogSupport, MethodMetaData}

import scala.collection.immutable.HashMap

/**
  * Created by 10192057 on 2018/4/11 0011.
  */
trait AbstractParser extends Configurable {
  protected val _srcFieldsInfo: ConcurrentHashMap[String, HashMap[String, Int]] = new ConcurrentHashMap[String, HashMap[String, Int]]()

  protected lazy val m_SrcFieldsInfo: HashMap[String, Int] = _srcFieldsInfo.getOrDefault("m_SrcColumnsFields", HashMap.empty)

  /**
    * Necessary Initialization / startup needed by the Parser.
    *
    * @param srcColumnsFields Raw data header
    */
  def initialize(srcColumnsFields: HashMap[String, Int]): Unit = {
    //m_SrcColumnsFields.putAll(srcColumnsFields)
    val temp: HashMap[String, Int] = new HashMap[String, Int]().++(srcColumnsFields)
    if (!_srcFieldsInfo.containsKey("m_SrcColumnsFields")) {
      _srcFieldsInfo.put("m_SrcColumnsFields", temp)
    }
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
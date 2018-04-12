package com.wallace.demo.app.parsercombinators.parsers

import java.util

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Created by 10192057 on 2018/4/11 0011.
  */
class ExtractFieldsAbstractParser extends AbstractParser {
  private val srcColumnsFields: mutable.Map[String, Int] = mutable.Map.empty
  private val tgtColumnsFields: mutable.Map[String, Int] = mutable.Map.empty
  private val m_SrcColumnsFields: util.HashMap[String, Int] = new util.HashMap[String, Int]
  m_SrcColumnsFields.putAll(srcColumnsFields.asJava)

  private val m_TgtColumnsFields: util.HashMap[String, Int] = new util.HashMap[String, Int]
  m_TgtColumnsFields.putAll(tgtColumnsFields.asJava)

  override def initialize(): Unit = {

  }

  override def parse(record: Array[String]): String = {
    ""
  }

  override def configure(context: MethodContext): Unit = {

  }
}

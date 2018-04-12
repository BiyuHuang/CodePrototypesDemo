package com.wallace.demo.app.parsercombinators.parsers

import java.util

import com.wallace.demo.app.common.FieldsSep

import scala.collection.JavaConverters._

/**
  * Created by 10192057 on 2018/4/12 0012.
  */
class ConcatFieldsAbstractParser extends AbstractParser {
  private val m_SrcColumnsFields: util.HashMap[String, Int] = new util.HashMap[String, Int]
  private val m_ConcatColumnsFields: util.HashMap[String, Array[String]] = new util.HashMap[String, Array[String]]()
  private var concat_sep: String = ""

  override def initialize(): Unit = {
    //no-op
  }

  override def parse(record: Array[String], fieldInfo: FieldInfo): String = {
    m_ConcatColumnsFields.get(fieldInfo.name).map {
      k =>
        record(m_SrcColumnsFields.get(k))
    }.mkString(concat_sep)
  }

  override def configure(context: MethodContext, srcColumnsFields: util.HashMap[String, Int]): Unit = {
    m_SrcColumnsFields.putAll(srcColumnsFields)

    val concatColumnsFields: Map[String, Array[String]] = {
      val key: String = context.methodMetaData.outputFields
      val value: Array[String] = context.methodMetaData.inputFields.split("\\s+|,")
      Map(key -> value)
    }

    m_ConcatColumnsFields.putAll(concatColumnsFields.asJava)
    concat_sep = context.methodMetaData.conf.getOrElse("separator", FieldsSep.DEFAULT_CONCAT_SEP)
  }
}

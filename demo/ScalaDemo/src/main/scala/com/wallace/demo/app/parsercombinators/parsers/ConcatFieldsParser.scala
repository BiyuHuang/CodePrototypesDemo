package com.wallace.demo.app.parsercombinators.parsers

import java.util

import scala.collection.JavaConverters._
import scala.collection.immutable.HashMap

/**
  * Created by 10192057 on 2018/4/12 0012.
  */
class ConcatFieldsParser extends AbstractParser {
  private val _concatColumnsFields: util.HashMap[String, Array[String]] = new util.HashMap[String, Array[String]]()
  private var concat_sep: String = ""

  private lazy val m_ConcatColumnsFields: HashMap[String, Array[String]] = new HashMap[String, Array[String]]().++(_concatColumnsFields.asScala)

  override def parse(record: Array[String], fieldInfo: FieldInfo): String = {
    if (m_ConcatColumnsFields.contains(fieldInfo.name)) {
      m_ConcatColumnsFields(fieldInfo.name)
        .map(k => if (m_SrcFieldsInfo.contains(k)) record(m_SrcFieldsInfo(k)) else "")
        .mkString(concat_sep)
    } else {
      ""
    }
  }

  override def configure(context: MethodContext): Unit = {
    val concatColumnsFields: Map[String, Array[String]] = {
      val key: String = context.methodMetaData.outputFields
      val value: Array[String] = context.methodMetaData.inputFields.split("\\s+|,")
      Map(key -> value)
    }

    _concatColumnsFields.putAll(concatColumnsFields.asJava)
    concat_sep = context.methodMetaData.conf("separator")
    if (concat_sep.isEmpty) {
      logger.warn(s"Concat Sep[$concat_sep] is empty.")
    }
  }
}

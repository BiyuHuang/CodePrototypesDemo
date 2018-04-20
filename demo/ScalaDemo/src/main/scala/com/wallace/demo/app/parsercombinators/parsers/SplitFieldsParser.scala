package com.wallace.demo.app.parsercombinators.parsers

import java.util

import com.wallace.demo.app.common.FieldsSep

import scala.collection.JavaConverters._
import scala.collection.immutable.HashMap
import scala.util.Try

/**
  * Created by 10192057 on 2018/4/12 0012.
  */
class SplitFieldsParser extends AbstractParser {
  private val _splitColumnsFields: util.HashMap[String, (String, Int)] = new util.HashMap[String, (String, Int)]()
  private var split_sep: String = ""

  private lazy val m_SplitColumnsFields: HashMap[String, (String, Int)] = new HashMap[String, (String, Int)]().++(_splitColumnsFields.asScala)

  override def parse(record: Array[String], fieldInfo: FieldInfo): String = {
    Try {
      val keyWithIndex: (String, Int) = m_SplitColumnsFields(fieldInfo.name)
      record(m_SrcFieldsInfo(keyWithIndex._1)).split(split_sep, -1)(keyWithIndex._2)
    }.getOrElse("")
  }

  override def configure(context: MethodContext): Unit = {
    split_sep = context.methodMetaData.conf.getOrElse("separator", FieldsSep.DEFAULT_SPLIT_SEP)
    val splitColumnsFields: Map[String, (String, Int)] = {
      val keyWithIndex: Array[(String, Int)] = context.methodMetaData.outputFields.split("\\s+|,").zipWithIndex
      val value: String = context.methodMetaData.inputFields

      keyWithIndex.flatMap {
        ki =>
          Map(ki._1 -> (value, ki._2))
      }.toMap
    }
    _splitColumnsFields.putAll(splitColumnsFields.asJava)
  }
}

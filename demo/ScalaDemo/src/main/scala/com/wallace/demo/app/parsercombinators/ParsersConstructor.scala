package com.wallace.demo.app.parsercombinators

import java.util

import com.wallace.demo.app.common._
import com.wallace.demo.app.parsercombinators.parsers._

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * Created by 10192057 on 2018/4/10 0010.
  */

case class ParserExecuteInfo(rawDataMetaData: RawDataMetaData,
                             m_SplitColumnsFields: util.HashMap[String, (String, Int, String)],
                             m_ConcatColumnsFields: util.HashMap[String, (Array[String], String)])

class ParsersConstructor(parsersConfig: Map[String, AlgMetaData]) extends Using {
  def generateParsers(): Map[String, ParserChain] = parsersConfig.map {
    parsersConf =>
      val m_Parsers: util.HashMap[String, AbstractParser] = new util.HashMap[String, AbstractParser]()
      val m_SrcColumnsFields: util.HashMap[String, Int] = new util.HashMap[String, Int]()
      val targetKey: String = parsersConf._1
      val parserMetaData: ParserMetaData = parsersConf._2.parsersMetaData
      val parsers: Map[String, MethodMetaData] = parserMetaData.parsers
      val srcColumnsFields: Map[String, Int] = Try(parserMetaData.inputFields.split(",", -1).map(_.trim).zipWithIndex.toMap[String, Int]).getOrElse(Map.empty)
      val tgtColumnsFields: Array[String] = Try(parserMetaData.outputFields.split(",", -1).map(_.trim)).getOrElse(Array.empty)
      val fieldsSep = Try(parserMetaData.fieldsSep).getOrElse(FieldsSep.DEFAULT_SEP)
      val m_TgtColumnsFields: Array[FieldInfo] = if (tgtColumnsFields.nonEmpty) {
        val specialFields: Map[String, String] = parsers.map {
          parser =>
            (parser._2.outputFields, parser._1)
        }
        val keys: String = specialFields.map(_._1.trim).mkString("|")
        tgtColumnsFields.zipWithIndex.map {
          elem =>
            val name = elem._1
            val index = elem._2
            val methodType = if (keys.contains(name)) {
              specialFields.map(item => if (item._1.contains(name)) item._2 else "").filter(_.nonEmpty).head
            } else {
              MethodKeyType.default
            }
            FieldInfo(name, index, methodType)
        }
      } else {
        Array.empty
      }
      if (srcColumnsFields.nonEmpty) m_SrcColumnsFields.putAll(srcColumnsFields.asJava)

      assert(!m_SrcColumnsFields.isEmpty && !m_TgtColumnsFields.isEmpty, "Failed to parse configuration file")
      val rawDataMetaData: RawDataMetaData = RawDataMetaData(fieldsSep, m_SrcColumnsFields, m_TgtColumnsFields)
      if (parsers.nonEmpty) {
        parsers.foreach {
          parser =>
            val context: MethodContext = MethodContext(parser._1, parser._2)
            val p = ParserFactory.newInstance(parser._1)
            p.configure(context, m_SrcColumnsFields)
            m_Parsers.put(parser._1, p)
        }
      }
      val extractFieldsParser = ParserFactory.newInstance(MethodKeyType.default)
      extractFieldsParser.configure(
        MethodContext(MethodKeyType.default, MethodMetaData(parserMetaData.inputFields, parserMetaData.outputFields, Map.empty)),
        m_SrcColumnsFields)
      m_Parsers.put(MethodKeyType.default, extractFieldsParser)

      val parserChain: ParserChain = new ParserChain(rawDataMetaData, m_Parsers)
      parserChain.initialize()
      (targetKey, parserChain)
  }
}

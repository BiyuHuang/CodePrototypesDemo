package com.wallace.demo.app.parsercombinators

import java.util

import com.wallace.demo.app.common._
import com.wallace.demo.app.parsercombinators.parsers._

import scala.collection.JavaConverters._
import scala.collection.immutable.HashMap
import scala.util.Try

/**
  * Created by 10192057 on 2018/4/10 0010.
  */

case class ParserExecuteInfo(rawDataMetaData: RawDataMetaData,
                             m_SplitColumnsFields: util.HashMap[String, (String, Int, String)],
                             m_ConcatColumnsFields: util.HashMap[String, (Array[String], String)])

object ParsersConstructor extends Using {
  def generateParsers(parsersConfig: Map[String, AlgMetaData]): Map[String, ParserChain] = parsersConfig.map {
    parsersConf =>
      val targetKey: String = parsersConf._1
      val parserMetaData: ParserMetaData = parsersConf._2.parsersMetaData
      if (parserMetaData.dataType.nonEmpty) {
        val srcColumnsFields: Map[String, Int] = Try(parserMetaData.inputFields.split(",", -1).map(_.trim).zipWithIndex.toMap[String, Int]).getOrElse(Map.empty)
        val tgtColumnsFields: Array[String] = Try(parserMetaData.outputFields.split(",", -1).map(_.trim)).getOrElse(Array.empty)
        val parsers: Map[String, MethodMetaData] = parserMetaData.parsers
        val m_Parsers: util.HashMap[String, AbstractParser] = new util.HashMap[String, AbstractParser]()
        val m_fieldsSep = Try(parserMetaData.fieldsSep).getOrElse(FieldsSep.DEFAULT_SEP)
        val m_SrcColumnsFields: HashMap[String, Int] = new HashMap[String, Int]().++(srcColumnsFields)
        val m_TgtColumnsFields: Array[FieldInfo] = if (tgtColumnsFields.nonEmpty) {
          val specialFields: Map[String, String] = Try(parsers.map {
            parser =>
              (parser._2.outputFields, parser._1)
          }).getOrElse(Map.empty)
          tgtColumnsFields.zipWithIndex.map {
            elem =>
              val name = elem._1
              val index = elem._2
              val methodType: String = if (specialFields.contains(name)) {
                specialFields(name)
              } else {
                MethodKeyType.default
              }
              FieldInfo(name, index, methodType)
          }
        } else {
          Array.empty
        }
        val rawDataMetaData: RawDataMetaData = RawDataMetaData(m_fieldsSep, m_SrcColumnsFields, m_TgtColumnsFields)
        if (parsers.nonEmpty) {
          parsers.foreach {
            parser =>
              val context: MethodContext = MethodContext(parser._1, parser._2)
              val p = ParserFactory.newInstance(parser._1)
              p.configure(context)
              m_Parsers.put(parser._1, p)
          }
        }
        val extractFieldsParser = ParserFactory.newInstance(MethodKeyType.default)
        extractFieldsParser.configure(MethodContext(MethodKeyType.default,
          MethodMetaData(parserMetaData.inputFields, parserMetaData.outputFields, Map.empty)))
        m_Parsers.put(MethodKeyType.default, extractFieldsParser)

        val parserChain: ParserChain = new ParserChain(rawDataMetaData, m_Parsers.asScala.toMap)
        parserChain.initialize()
        (targetKey, parserChain)
      } else {
        (targetKey, null)
      }
  }
}

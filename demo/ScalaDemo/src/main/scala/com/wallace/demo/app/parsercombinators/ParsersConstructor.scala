package com.wallace.demo.app.parsercombinators

import java.util

import com.wallace.demo.app.common._

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * Created by 10192057 on 2018/4/10 0010.
  */

case class RawDataMetaData(fieldsSep: String,
                           m_SrcColumnsFields: util.HashMap[String, Int],
                           m_TgtColumnsFields: util.ArrayList[String])

case class ParserExecuteInfo(rawDataMetaData: RawDataMetaData,
                             m_SplitColumnsFields: util.HashMap[String, (String, Int, String)],
                             m_ConcatColumnsFields: util.HashMap[String, (Array[String], String)])

class ParsersConstructor(parsersConfig: Map[String, AlgMetaData]) extends Using {
  val m_Parser: util.HashMap[String, ParserExecuteInfo] = new util.HashMap[String, ParserExecuteInfo]()

  def generateParsers(): Unit = {
    parsersConfig.foreach {
      parsersConf =>
        val m_SrcColumnsFields: util.HashMap[String, Int] = new util.HashMap[String, Int]()
        val m_TgtColumnsFields: util.ArrayList[String] = new util.ArrayList[String]()
        val m_SplitColumnsFields: util.HashMap[String, (String, Int, String)] = new util.HashMap[String, (String, Int, String)]()
        val m_ConcatColumnsFields: util.HashMap[String, (Array[String], String)] = new util.HashMap[String, (Array[String], String)]()
        val targetKey = parsersConf._1
        val parserMetaData: ParserMetaData = parsersConf._2.parsersMetaData

        val srcColumnsFields: Map[String, Int] = Try(parserMetaData.inputFields.split(",", -1).map(_.trim).zipWithIndex.toMap[String, Int]).getOrElse(Map.empty)
        val tgtColumnsFields: Array[String] = Try(parserMetaData.outputFields.split(",", -1).map(_.trim)).getOrElse(Array.empty)
        val fieldsSep = Try(parserMetaData.fieldsSep).getOrElse(FieldsSep.DEFAULT_SEP)

        val parsers: Map[String, MethodMetaData] = parserMetaData.parsers

        if (tgtColumnsFields.nonEmpty) tgtColumnsFields.foreach(m_TgtColumnsFields.add)
        if (srcColumnsFields.nonEmpty) m_SrcColumnsFields.putAll(srcColumnsFields.asJava)
        if (parsers.nonEmpty) {
          //          m_Parser.putAll(parsers.asJava)
          parsers.foreach {
            parser =>
              val parserName: String = parser._1
              val metaData: MethodMetaData = parser._2
              parserName match {
                case "split" =>
                  val keyWithIndex = metaData.outputFields.split(",", -1).zipWithIndex
                  val value = metaData.inputFields
                  val sep: String = metaData.conf.getOrElse("separator", FieldsSep.DEFAULT_SEP)
                  val res: util.Map[String, (String, Int, String)] = keyWithIndex.flatMap {
                    ki =>
                      Map(ki._1 -> (value, ki._2, sep))
                  }.toMap.asJava
                  m_SplitColumnsFields.putAll(res)
                case "concat" =>
                  val key: String = metaData.outputFields
                  val value: Array[String] = metaData.inputFields.split(",")
                  val sep = metaData.conf.getOrElse("separator", FieldsSep.DEFAULT_CONCAT_SEP)
                  val res: util.Map[String, (Array[String], String)] = Map(key -> (value, sep)).asJava
                  m_ConcatColumnsFields.putAll(res)
              }
          }
        }
        m_Parser.put(targetKey,
          ParserExecuteInfo(RawDataMetaData(fieldsSep, m_SrcColumnsFields, m_TgtColumnsFields),
            m_SplitColumnsFields,
            m_ConcatColumnsFields))
    }
  }
}

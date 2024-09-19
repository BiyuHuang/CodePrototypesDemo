package com.wallace.demo.app.common

import com.typesafe.config.Config

/**
  * Created by 10192057 on 2018/4/10 0010.
  */
sealed trait MetaData

case class AlgMetaData(algArgs: Config, className: String, parsersMetaData: ParserMetaData) extends MetaData {
  override def toString: String = s"""AlgArgs: ${algArgs.toString}, ClassName: $className, ParsersMetaData: ${parsersMetaData.toString}"""
}

case class MethodMetaData(inputFields: String, outputFields: String, conf: Map[String, String]) extends MetaData {
  override def toString: String = s"MethodMetaData: InputFields => $inputFields, OutputFields => $outputFields, Conf: ${conf.toString()}"
}

case class ParserMetaData(dataType: String, fieldsSep: String, inputFields: String, outputFields: String,
                          parsers: Map[String, MethodMetaData]) extends MetaData {
  //  def isEmpty: Boolean = this match {
  //    case _: ParserMetaData => false
  //    case _ => true
  //  }

  override def toString: String =
    s"""|InputFields: $inputFields
        |OutputFields: $outputFields
        |Parsers: ${parsers.mkString("\n")}""".stripMargin
}

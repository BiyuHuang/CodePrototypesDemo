package com.wallace.demo.app.parsercombinators.parsers

import com.wallace.demo.app.utils.MathUtils

/**
  * Created by 10192057 on 2018/4/13 0013.
  */
class ComputeFieldsParser extends AbstractParser {

  private var inputSystem: String = ""
  private var outputSystem: String = ""
  private var operator: String = ""
  private var operand: String = ""

  override def parse(record: Array[String], field: FieldInfo): String = {
    if (m_SrcColumnsFields.containsKey(field.name)) {
      val data: String = record(m_SrcColumnsFields.get(field.name))
      if (data.contains(".")) {
        MathUtils.execOperations(data.toDouble, operator, operand.toDouble).toString
      } else {
        val temp = MathUtils.execOperations(MathUtils.parseInt(data, inputSystem), operator, operand.toInt)
        MathUtils.formatValue(temp, outputSystem)
      }
    } else {
      ""
    }

  }

  override def configure(context: MethodContext): Unit = {
    inputSystem = context.methodMetaData.conf.getOrElse("inputsystem", "d")
    outputSystem = context.methodMetaData.conf.getOrElse("outputsystem", "d")
    operator = context.methodMetaData.conf.getOrElse("operator", "")
    operand = context.methodMetaData.conf.getOrElse("operand", "0")

    require(operator.nonEmpty, "operator must be non-empty")
  }
}

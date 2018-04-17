package com.wallace.demo.app.parsercombinators.parsers

/**
  * Created by 10192057 on 2018/4/13 0013.
  */
class ReplaceStrFieldsParser extends AbstractParser {
  private var regex: String = ""
  private var replacement: String = ""

  /**
    * Parsed of a single line of Record.
    *
    * @param record numeric fields of Record to be parsed
    * @return single numeric field
    */
  override def parse(record: Array[String], field: FieldInfo): String = {
    if (m_SrcColumnsFields.containsKey(field.name)) {
      record(m_SrcColumnsFields.get(field.name)).replaceAll(regex, replacement)
    } else {
      ""
    }
  }

  /**
    * @param context MethodContext
    **/
  override def configure(context: MethodContext): Unit = {
    replacement = context.methodMetaData.conf.getOrElse("target", "")
    regex = context.methodMetaData.conf.getOrElse("source", "")
    assert(regex.nonEmpty, "Replace Regex[$regex] is empty.")
  }
}

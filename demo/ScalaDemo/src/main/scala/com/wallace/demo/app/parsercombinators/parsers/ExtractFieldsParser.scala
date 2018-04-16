package com.wallace.demo.app.parsercombinators.parsers

/**
  * Created by 10192057 on 2018/4/11 0011.
  */
class ExtractFieldsParser extends AbstractParser {
  override def parse(record: Array[String], field: FieldInfo): String = {
    if (m_SrcColumnsFields.containsKey(field.name)) {
      record(m_SrcColumnsFields.get(field.name))
    } else {
      ""
    }
  }

  override def configure(context: MethodContext): Unit = {
    //no-op
  }
}

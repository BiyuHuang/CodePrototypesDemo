package com.wallace.demo.app.parsercombinators.parsers

/**
  * Created by 10192057 on 2018/4/11 0011.
  */
class ExtractFieldsParser extends AbstractParser {
  override def parse(record: Array[String], fieldInfo: FieldInfo): String = {
    record(m_SrcColumnsFields.get(fieldInfo.name))
  }

  override def configure(context: MethodContext): Unit = {
    //no-op
  }
}

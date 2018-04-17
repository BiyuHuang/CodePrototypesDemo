package com.wallace.demo.app.parsercombinators.parsers

/**
  * Created by 10192057 on 2018/4/13 0013.
  */
class AddTimeStampFieldsParser extends AbstractParser {
  /**
    * Parsed of a single line of Record.
    *
    * @param record numeric fields of Record to be parsed
    * @return single numeric field
    */
  override def parse(record: Array[String], filed: FieldInfo): String = {
    ""
  }

  /**
    * @param context MethodContext
    **/
  override def configure(context: MethodContext): Unit = {

  }
}

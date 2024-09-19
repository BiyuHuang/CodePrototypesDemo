package com.wallace.demo.app.utils

/**
  * com.wallace.demo.app.utils
  * Created by 10192057 on 2018/2/22 0022.
  */
trait SqlText {
  protected def insertIntoTabSql(insertSqlMetaData: SqlMetaData, isOverWrite: Boolean = false): String = {
    val partInfo: String = if (insertSqlMetaData.optionalPartInfo.isDefined) s"PARTITION(${insertSqlMetaData.optionalPartInfo.get})" else ""
    val insertMode = if (isOverWrite) "OVERWRITE" else "INTO"
    val selectSqlText: String = selectFieldsSql(insertSqlMetaData)
    s"""
       |INSERT $insertMode TABLE ${insertSqlMetaData.tgtTab} $partInfo
       |$selectSqlText
       |""".stripMargin
  }

  protected def selectFieldsSql(selectSqlMetaData: SqlMetaData): String = {
    val condition: String = if (selectSqlMetaData.optionalCondition.isDefined) s"WHERE ${selectSqlMetaData.optionalCondition.get}" else ""
    s"""|SELECT ${selectSqlMetaData.fields} FROM ${selectSqlMetaData.srcTab}
        |$condition""".stripMargin
  }
}

case class SqlMetaData(srcTab: String, tgtTab: String, fields: String, optionalPartInfo: Option[String], optionalCondition: Option[String])
package com.wallace.demo.app.utils

import com.wallace.demo.app.common.LogSupport

/**
  * Created by wallacehuang on 2017/5/13.
  */
class CombineMultiVersion extends LogSupport {
  def combineMultiVersion(toBeRepairedColStr: String, toBeRepairedCol: Array[String]): String = {
    val originSQLText = toBeRepairedColStr
    val temp: Array[(String, Int)] = originSQLText.split(",", -1).map(_.trim.toLowerCase).zipWithIndex
    for (colName <- toBeRepairedCol) {
      val index = colIndex(temp, colName)
      logger.debug(s"[RepairedColIndex]: $index")
      if (index > 0) {
        val repairedValue = repairColValue(colName)
        logger.debug(s"[RepairedValue]: $repairedValue")
        temp.update(index, (repairedValue, index))
      } else {
        logger.error(
          s"""
             |#########################################################################
             |[CombineMultiVersion] => Index of #$colName#: $index, it was Out Of Range.
             |[CombineMultiVersion] => Please check the correct column name in <$originSQLText>
             |#########################################################################
             |""".stripMargin)
      }
    }
    temp.map(_._1).mkString(",")
  }

  protected def colIndex(pairValue: Array[(String, Int)], colName: String): Int = pairValue.toMap.getOrElse(colName.trim, -1)


  protected def repairColValue(colName: String): String = {
    val tempCol = colName.trim.toLowerCase
    tempCol match {
      //TODO: If you want to change any column's value ,please add the user defined rules for repair column's value.
      case "rsrp" => s"rsrp_repaired as $tempCol"
      case "rsrq" => s"rsrq_repaired as $tempCol"
      case _ => s"$tempCol"
    }
  }
}

object CombineMultiVersion {
  def apply: CombineMultiVersion = new CombineMultiVersion()
}

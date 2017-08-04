package com.wallace.snmp

import java.io.PrintWriter

import com.wallace.common.LogSupport
import com.wallace.demo.EncodingParser

import scala.collection.mutable.ListBuffer
import scala.io.Source.fromFile
import scala.util.Try

/**
  * Created by Wallace on 2016/8/19.
  */
class SnmpPDUAnalysis(path: String) extends LogSupport {
  protected val _path: String = path.replaceAll("\\\\", "/")

  def process(): List[String] = {
    val fileReader = fromFile(this._path, "UTF-8")
    val fileLines = fileReader.getLines().toArray

    var alarmId_Size: Int = 0
    var alarmCode_Size: Int = 0
    var alarmText_Size: Int = 0
    var alarmInfo_Size: Int = 0

    val alarmId: ListBuffer[Map[Int, String]] = new ListBuffer[Map[Int, String]]
    val alarmCode: ListBuffer[Map[Int, String]] = new ListBuffer[Map[Int, String]]
    val alarmText: ListBuffer[Map[Int, String]] = new ListBuffer[Map[Int, String]]
    val alarmInfo: ListBuffer[Map[Int, String]] = new ListBuffer[Map[Int, String]]
    val alarmIterator: ListBuffer[Int] = new ListBuffer[Int]()
    try {
      fileLines.indices.foreach {
        x =>
          val line = fileLines(x).drop(27).dropRight(37).split(",vbs=", -1)
          val index: Array[Int] = line(0).split("=")(1).split("\\.").map(x => x.toInt)
          index(0) match {
            case 2 => alarmId.append(Map(-1 -> ""))
            case 4 => alarmCode.append(Map(-1 -> ""))
            case 8 => alarmText.append(Map(-1 -> ""))
            case 16 => alarmInfo.append(Map(-1 -> ""))
            case _ => log.warn("[Warning]: No Match.")
          }
      }

      (0 until List(alarmId.size, alarmCode.size, alarmText.size, alarmInfo.size).max).foreach(x => alarmIterator.append(x))
      fileLines.indices.foreach {
        x =>
          val line = fileLines(x).drop(27).dropRight(37).split(",vbs=", -1)
          val index: Array[Int] = line(0).split("=")(1).split("\\.").map(x => x.toInt)
          val value = line(1).drop(1).dropRight(1).split("\\ =\\ ")
          index(0) match {
            case 2 =>
              alarmId.update(alarmId_Size, Map(index(1) -> Try(value(1)).getOrElse("")))
              alarmIterator.update(alarmId_Size, index(1))
              alarmId_Size += 1
            case 4 =>
              alarmCode.update(alarmCode_Size, Map(index(1) -> Try(value(1)).getOrElse("")))
              alarmIterator.update(alarmCode_Size, index(1))
              alarmCode_Size += 1
            case 8 =>
              val tempValue: String = if (Try(value(1)).getOrElse("").contains(":") && !Try(value(1)).getOrElse("").contains("=") && !Try(value(1)).getOrElse("").contains("-")) EncodingParser.toStringHex(Try(value(1)).getOrElse("").replaceAll(":", ""), "gbk").replaceAll("\n", "") else Try(value(1)).getOrElse("")
              alarmText.update(alarmText_Size, Map(index(1) -> tempValue))
              alarmIterator.update(alarmText_Size, index(1))
              alarmText_Size += 1
            case 16 =>
              alarmInfo.update(alarmInfo_Size, Map(index(1) -> Try(value(1)).getOrElse("")))
              alarmIterator.update(alarmInfo_Size, index(1))
              alarmInfo_Size += 1
            case _ => log.warn("[Warning]: No Match.")
          }
      }
    } catch {
      case e: Throwable =>
        log.error("[Throw new exception]", e)
        throw e
    }

    val fileWrite = new PrintWriter("AlarmTable.csv", "UTF-8")
    val alarmTableList = new ListBuffer[String]

    val keyIterator = alarmIterator.result()
    try {
      keyIterator.indices.foreach {
        i =>
          var col_1 = ""
          var col_2 = ""
          var col_3 = ""
          var col_4 = ""
          col_1 = alarmId(i).getOrElse(keyIterator(i), "")
          col_2 = alarmCode(i).getOrElse(keyIterator(i), "")
          col_3 = alarmText(i).getOrElse(keyIterator(i), "")
          col_4 = alarmInfo(i).getOrElse(keyIterator(i), "")
          val record = s"$col_1|$col_2|$col_3|$col_4"
          alarmTableList.append(record)
          fileWrite.write(record + "\n")
          fileWrite.flush()

      }
    }
    catch {
      case e: Throwable =>
        log.error("[Throw new exception]", e)
        throw e
    }
    val alarmTableData: List[String] = alarmTableList.result()
    fileReader.close()
    fileWrite.close()
    alarmTableData
  }
}

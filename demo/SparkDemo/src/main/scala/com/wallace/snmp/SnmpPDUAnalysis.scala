/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.snmp

import java.io.PrintWriter

import com.wallace.common.LogSupport
import com.wallace.demo.EncodingParser

import scala.collection.mutable.ListBuffer
import scala.io.BufferedSource
import scala.io.Source.fromFile
import scala.util.Try

/**
  * Created by Wallace on 2016/8/19.
  */
class SnmpPDUAnalysis(path: String) extends LogSupport {
  protected val _path: String = path.replaceAll("\\\\", "/")
  private var alarmId_Size: Int = 0
  private var alarmCode_Size: Int = 0
  private var alarmText_Size: Int = 0
  private var alarmInfo_Size: Int = 0
  private val alarmId: ListBuffer[Map[Int, String]] = new ListBuffer[Map[Int, String]]
  private val alarmCode: ListBuffer[Map[Int, String]] = new ListBuffer[Map[Int, String]]
  private val alarmText: ListBuffer[Map[Int, String]] = new ListBuffer[Map[Int, String]]
  private val alarmInfo: ListBuffer[Map[Int, String]] = new ListBuffer[Map[Int, String]]
  private val alarmIterator: ListBuffer[Int] = new ListBuffer[Int]()
  val fileWrite = new PrintWriter("AlarmTable.csv", "UTF-8")
  val alarmTableList = new ListBuffer[String]
  val fileReader: BufferedSource = fromFile(this._path, "UTF-8")
  val fileLines: Array[String] = fileReader.getLines().toArray

  protected def beforeProcess(): Unit = {
    fileLines.indices.foreach { x =>
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
  }

  protected def afterProcess(): Unit = {
    val keyIterator = alarmIterator.result()
    keyIterator.indices.foreach { i =>
      var col1 = ""
      var col2 = ""
      var col3 = ""
      var col4 = ""
      col1 = alarmId(i).getOrElse(keyIterator(i), "")
      col2 = alarmCode(i).getOrElse(keyIterator(i), "")
      col3 = alarmText(i).getOrElse(keyIterator(i), "")
      col4 = alarmInfo(i).getOrElse(keyIterator(i), "")
      val record = s"$col1|$col2|$col3|$col4"
      alarmTableList.append(record)
      fileWrite.write(record + "\n")
      fileWrite.flush()
    }
  }

  protected def updateProcess(): Unit = {
    fileLines.indices.foreach { x =>
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
          val tempValue: String = if (Try(value(1)).getOrElse("").contains(":") &&
            !Try(value(1)).getOrElse("").contains("=") &&
            !Try(value(1)).getOrElse("").contains("-")) {
            EncodingParser.toStringHex(Try(value(1)).getOrElse("").replaceAll(":", ""), "gbk").replaceAll("\n", "")
          } else {
            Try(value(1)).getOrElse("")
          }
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
  }

  def process(): List[String] = {
    try {
      beforeProcess()
      updateProcess()
      afterProcess()
      alarmTableList.result()
    } catch {
      case e: Throwable =>
        log.error("[Throw new exception]", e)
        alarmTableList.result()
    } finally {
      fileReader.close()
      fileWrite.close()
    }
  }
}

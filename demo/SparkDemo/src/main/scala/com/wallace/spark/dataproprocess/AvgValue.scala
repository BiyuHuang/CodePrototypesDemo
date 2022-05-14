package com.wallace.spark.dataproprocess

import com.wallace.common.LogSupport
import com.wallace.common.timeformat.TimePara

import scala.io.Source

/**
 * Created by Wallace on 2016/5/6.
 * 计算一列数据的平均值，个数，总和
 */
object AvgValue extends App with LogSupport {
  val inputFile = Source.fromFile(s"./demo/SparkDemo/data/AvgValue_${TimePara.getDatePartition}_TestSpendTime.csv")
  val lines: Array[String] = inputFile.getLines().toArray
  val sum: Double = lines.map(_.toDouble).sum
  log.error(s"[${TimePara.getCurrentDate}] Avg value: ${sum / lines.length}," +
    s" Total numbers: ${lines.length}, Sum value: $sum    .")
}

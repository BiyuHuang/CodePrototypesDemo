package com.wallace.spark.DataProcess

import java.text.SimpleDateFormat
import java.util.Date

import com.wallace.spark.common.LogSupport
import com.wallace.spark.common.TimeFormat.TimePara

import scala.io.Source

/**
  * Created by Wallace on 2016/5/6.
  * 计算一列数据的平均值，个数，总和
  */
object AvgValue extends App with LogSupport {
  val inputFile = Source.fromFile(s"./data/AvgValue_${new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis))}_TestSpendTime.csv")
  val lines = inputFile.getLines().toArray
  var sum = 0.0
  (1 to lines.length).foreach(i => sum += lines(i - 1).toDouble)
  log.error(s"[${TimePara.getCurrentDate}] Avg value: ${sum / lines.length}, Total numbers: ${lines.length}, Sum value: $sum    .")

}

package com.wallace.spark.DataProProcess

import java.io.PrintWriter

import com.wallace.common.LogSupport
import com.wallace.common.TimeFormat.TimePara

import scala.collection.mutable.ArrayBuffer
import scala.util.Random


/**
  * Created by Wallace on 2016/5/5.
  * Produce Data And Save File on Local Path
  */
object DataProducer extends LogSupport {
  def dataProducer(num: Int): Array[String] = {
    val random = new Random
    val ueId = Array("10001", "10002", "10003", "10004", "10005", "10006", "10007", "10008", "10009", "10010", "10011", "10012", "10013", "10014")
    val text = Array("aaaaaaaaaaa", "bbbbbbbbb", "cccccccccccc", "dddddddddddd", "fffffffffffffff", "gggggggggggg", "hhhhhhhhhhhhh")
    val (region_id_c, x_offset_c, y_offset_c) = (4841, 0, 0)

    val resBuffer = new ArrayBuffer[String]()
    (0 until num).foreach {
      x =>
        val factor = if (x == 0) 0 else random.nextInt(x)
        val record = s"""${TimePara.getCurrentTime},${ueId(factor % ueId.length)},${text(factor % text.length)},${region_id_c + factor},${x_offset_c + factor + x},${y_offset_c + factor + x}\n"""
        resBuffer.append(record)
    }
    resBuffer.result().toArray
  }

  def main(args: Array[String]) {
    /** val eNodeBID = List("614255,49", "614234,40", "615234,1", "625121,32", "623001,33")
      * val random = new Random
      *random.setSeed(10)
      * val score: Double = random.nextDouble
      * val lon: Float = random.nextFloat
      * val lat: Float = random.nextFloat
      * val sign = Array("true", "false", "null")
      * *
      * val dateStyle = new SimpleDateFormat("yyyy-MM-dd hh:MM:ss.SSS")
      * val df: DecimalFormat = new DecimalFormat("######0.000")
      * val writer = new FileWriter(new File(s"./data/DateProducer_${new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis - 24 * 60 * 60 * 1000))}_Test.csv"))
      * for (i <- 0 to 10000) {
      * val str: String = s"${dateStyle.format(new Date(System.currentTimeMillis - 24 * 60 * 60 * 1000))},${eNodeBID(random.nextInt(5))},${df.format(score * 10)},${df.format(lon * 100 + 50.0)},${df.format(lat * 100 + 100.0)},${sign(random.nextInt(3))}\n"
      *log.error(s"####### str = $str")
      *writer.write(str)
      *Thread.sleep(0)
      * }
      *writer.close()
      */


    /**
      * //读取本地文件
      * val file = Source.fromFile(s"./data/DateProducer_${new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis - 24 * 60 * 60 * 1000))}_Test.csv", "UTF-8")
      * val lines = file.getLines.toArray
      * while (lines.length >= 0) {
      * val startTime = System.currentTimeMillis()
      * (0 to 10000).foreach { messageNum =>
      * val str = lines(Random.nextInt(lines.length))
      *str.split(",", -1).toList
      *log.error(s"${str.split(",", -1).toList}")
      * }
      * val endTime = System.currentTimeMillis()
      * val costTime = endTime - startTime
      *log.error(s"[Costing Time]: $costTime ms")
      * val outputFile = new File(s"./data/AvgValue_${TimePara.getDatePartition}_TestSpendTime.csv")
      * val out = new FileWriter(outputFile, true)
      *out.write(costTime.toInt + "\n")
      *out.close()
      *Thread.sleep(1000)
      * }
      * *
      *file.close()
      */

    //网络资源读取
    //    val webFile=Source.fromURL("http://spark.apache.org")
    //    webFile.foreach(print)
    //    webFile.close()

    val printWriter = new PrintWriter(s"./demo/SparkDemo/data/Testing_Data_${TimePara.getDatePartition}.csv", "utf-8")
    val resData = dataProducer(10000)
    resData.indices.foreach {
      x =>
        printWriter.append(resData(x))
    }
    printWriter.flush()
    printWriter.close()
  }
}

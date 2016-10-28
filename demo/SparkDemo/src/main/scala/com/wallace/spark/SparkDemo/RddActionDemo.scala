package com.wallace.spark.SparkDemo

import java.text.{DecimalFormat, SimpleDateFormat}

import com.wallace.spark.common.LogSupport
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * Created by Wallace on 2016/5/10.
  * Rdd Action
  */
object RddActionDemo extends LogSupport {
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local[*]").setAppName("ActionDemo")
    val sc = new SparkContext(conf)

    val file = Source.fromFile("./data/DateProducer_2016-05-09_Test.csv", "UTF-8")
    val line = file.getLines().toArray
    val dateStyle = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    val msgBuffer: ArrayBuffer[List[String]] = ArrayBuffer(List(null))
    for (i <- line.indices) {
      val record = line(i)
      val msg = record
      msgBuffer.append(msg.split(",", -1).toList)
      //      log.error(s"###### History msg : $msg")
      //log.error(s"###### Buffer mss : $msgBuffer")
    }

    //小数位数保留
    val df: DecimalFormat = new DecimalFormat("######0.00")

    val rdd1 = sc.makeRDD(msgBuffer.drop(1), 5)
    val rdd2 = rdd1.map(ls => s"""('${ls.toString.drop(5).dropRight(1)}')""".replaceFirst(",", "',").reverse.replaceFirst(" ", "").replaceFirst(",", "',").reverse)
    //    val rdd3 = rdd2.map(ls => s"""($ls)""")

    log.error(s"###### RDD1: ${rdd1.collect.toList.take(10)}")
    log.error(s"###### RDD2: ${rdd2.collect.toList.take(10)}")
    //    log.error(s"###### RDD3: ${rdd3.collect.toList.take(10)}")

    val data: String = rdd2.reduce(_ + "," + _)
    log.error(s"###### data: $data")
    file.close()
    sc.stop()
  }
}

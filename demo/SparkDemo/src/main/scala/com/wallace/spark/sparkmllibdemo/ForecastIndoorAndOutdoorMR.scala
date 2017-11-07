/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.spark.sparkmllibdemo

import com.wallace.spark.CreateSparkSession
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by wallace on 2017/11/7.
  */
object ForecastIndoorAndOutdoorMR extends CreateSparkSession {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = createSparkSession("ForecastIndoorAndOutdoorMRDemo")
    spark.sql("use default")
    val rdd = spark.sparkContext.textFile("./demo/SparkDemo/src/main/resources/trainingData.csv")
    import spark.implicits._


    val splitRdds: Array[RDD[String]] = rdd.randomSplit(Array(0.7, 0.3), seed = 11L) //Split data into training (70%) and test(30%)
    val srcDF: DataFrame = splitRdds.head.map(x => x.split(",")).map(e =>
      DataFields(e(0), e(1).trim.toInt, e(2).trim.toInt, e(3).trim.toDouble, e(4).trim.toDouble, e(5).trim.toDouble, e(6).trim.toInt,
        e(7).trim.toInt, e(8), e(9), e(10), e(11), e(12), e(13), e(14).trim.toInt)).toDF()
    srcDF.show(1)


  }


  private case class DataFields(reportCellKey: String, strongestNBPci: Int, aoa: Int, ta_calc: Double, rsrp: Double, rsrq: Double,
                                ta: Int, taDltValue: Int, mrtime: String, imsi: String, ndsKey: String, ueRecordID: String,
                                startTime: String, endTime: String, positionMarkReal: Int)

}

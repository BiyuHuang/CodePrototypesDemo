/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.spark.sparkdemo.exam

import com.wallace.common.{CreateSparkSession, Using}
import com.wallace.utils.DateUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.hive.HiveContext

import scala.util.Try

/**
  * Created by wallace on 2019/1/27.
  */
object SparkExamDemo extends CreateSparkSession with Using {
  def main(args: Array[String]): Unit = {
    usingSpark(createSparkSession("SparkExamDemo")) {
      spark =>
        val hc: HiveContext = new HiveContext(spark.sparkContext)
        exam1(hc, "test_table_1", "2019-01-28")
        exam2(hc, "test_table_2", "2019-01-28")
    }
  }

  def exam1(hc: HiveContext, srcTab: String, endDate: String, nDays: Int = 60): Unit = {
    // tab1
    val startDate10 = DateUtils.addDate(endDate, -10)
    val startDate20 = DateUtils.addDate(endDate, -20)
    val startDate30 = DateUtils.addDate(endDate, -30)
    val startDate60 = DateUtils.addDate(endDate, -nDays)
    val srcData: RDD[String] = hc.sql(
      s"""
         |select register_date
         |from $srcTab
         |where register_date >= '$startDate60'
         |  and register_date < '$endDate'
       """.stripMargin)
      .rdd.map(row => Try(row.getString(0)).getOrElse(""))

    srcData.persist()

    println("On the past 60 days: " + srcData.count() + " records.")
    println("On the past 30 days: " + srcData.filter(_ >= startDate30).count() + " records.")
    println("On the past 20 days: " + srcData.filter(_ >= startDate20).count() + " records.")
    println("On the past 10 days: " + srcData.filter(_ >= startDate10).count() + " records.")
  }

  def exam2(hc: HiveContext, srcTab: String, endDate: String, nDays: Int = 30): Unit = {
    val startDate30: String = DateUtils.addDate(endDate, -nDays)

    val srcData: RDD[String] = hc.sql(
      s"""
         |select sdl_created_date
         |from $srcTab
         |where sdl_created_date >= '$startDate30'
         |  and sdl_created_date < '$endDate'
       """.stripMargin)
      .rdd.map(row => Try(row.getString(0).split(" ", -1).head).getOrElse(""))

    srcData.map(x => (x, 1))
      .reduceByKey(_ + _)
      .saveAsTextFile("/temp/result_exam2/")
  }
}

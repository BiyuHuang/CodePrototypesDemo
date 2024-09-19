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
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext

import scala.util.Try

/**
 * Created by wallace on 2019/1/27.
 */
object SparkExamDemo extends CreateSparkSession with Using {
  def main(args: Array[String]): Unit = {
    usingSpark(createSparkSession("SparkExamDemo")) {
      spark =>
        val hc: SQLContext = spark.sqlContext
        exam1(hc, "test_table_1", "2019-01-28")
        exam2(hc, "test_table_2", "2019-01-28")
        exam3(hc, "test_table_3")
        exam4(hc, "test_table_4", "test_table_5")
        exam5(hc, "test_table_6", "test_table_7")
    }
  }

  def exam1(hc: SQLContext, srcTab: String, endDate: String, nDays: Int = 60): Unit = {
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

  def exam2(hc: SQLContext, srcTab: String, endDate: String, nDays: Int = 30): Unit = {
    val startDate30: String = DateUtils.addDate(endDate, -nDays)

    val srcData: RDD[String] = hc.sql(
      s"""
         |select sdl_created_date
         |from $srcTab
         |where sdl_created_date >= '$startDate30'
         |  and sdl_created_date < '$endDate'
       """.stripMargin)
      .rdd.map(row => Try(row.getString(0).split(" ", -1).head).getOrElse(""))

    val resData: RDD[(String, Int)] = srcData
      .map(x => (x, 1))
      .reduceByKey(_ + _)
    resData.saveAsTextFile("/temp/result_exam2/")
  }

  def exam3(hc: SQLContext, srcTab: String): Unit = {
    val srcData: RDD[(String, String)] = hc.sql(s"""select epass_no,activity_type from $srcTab""".stripMargin)
      .rdd.map(row => (Try(row.getString(0)).getOrElse(""), Try(row.getString(1)).getOrElse("")))

    val resData: RDD[(String, Int)] = srcData.map {
      line =>
        line._2 match {
          case "长期高频" => ("优质用户", 1)
          case "长期低频" => ("普通用户", 1)
          case "短期高频" => ("普通用户", 1)
          case _ => ("其他用户", 1)
        }
    }.reduceByKey(_ + _)

    resData.saveAsTextFile("/temp/result_exam3")
  }


  def exam4(hc: SQLContext, srcTab1: String, srcTab2: String): Unit = {
    val joinData: Map[String, Int] = hc.sql(s"""select client_id from $srcTab1 where model_type = 'JGJ_PRE'""".stripMargin)
      .rdd.map(row => Try(row.getString(0)).getOrElse("")).map(x => (x, 1)).collectAsMap().toMap

    val joinDataBC: Broadcast[Map[String, Int]] = hc.sparkContext.broadcast(joinData)

    val resData: RDD[(String, String)] = hc.sql(s"""select phone_no,is_zn from $srcTab2""")
      .rdd.map(row => (Try(row.getString(0)).getOrElse(""), Try(row.getString(1)).getOrElse("")))
      .mapPartitions {
        iter =>
          val joinData: Map[String, Int] = joinDataBC.value
          iter.map {
            line =>
              val (key, value) = (line._1, line._2)
              val preProduct: String = value match {
                case "1" => s"${value.toInt + 100}"
                case _ => "PF"
              }
              if (joinData.contains(key)) {
                (key, preProduct)
              } else {
                ("", preProduct)
              }
          }
      }.filter(_._1.nonEmpty).repartition(50)

    resData.saveAsTextFile("/temp/result_exam4/")
  }

  def exam5(hc: SQLContext, srcTab1: String, srcTab2: String): Unit = {
    val leftData: RDD[((String, String), Int)] = hc.sql(s"""select mobile_no, action_menu from $srcTab1""").rdd
      .map {
        row =>
          val key: (String, String) = (Try(row.getString(0)).getOrElse(""), Try(row.getString(1)).getOrElse(""))
          (key, 1)
      }

    val rightData = hc.sql(
      s"""select mobile_no,
         |       action_menu,
         |       total_action_cnt,
         |       total_action_duration
         |  from $srcTab2""".stripMargin).rdd
      .map {
        row =>
          val key: (String, String) = (Try(row.getString(0)).getOrElse(""), Try(row.getString(1)).getOrElse(""))
          val value: (String, String) = (Try(row.getString(2)).getOrElse(""), Try(row.getString(3)).getOrElse(""))
          (key, value)
      }

    val resData: RDD[(String, String, String, String, String)] = leftData.leftOuterJoin(rightData).map {
      row =>
        val key: (String, String) = row._1
        val tempVal: (String, String) = row._2._2.getOrElse((null, null))
        ("2019-01-25", key._1, key._2, tempVal._1, tempVal._2)
    }

    resData.saveAsTextFile("/temp/result_exam5/")
  }
}

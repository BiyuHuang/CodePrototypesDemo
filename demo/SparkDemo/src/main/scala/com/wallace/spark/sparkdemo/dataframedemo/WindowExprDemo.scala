/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.spark.sparkdemo.dataframedemo

import com.wallace.common.CreateSparkSession
import org.apache.spark.sql._
import org.apache.spark.sql.expressions.{Window, WindowSpec}
import org.apache.spark.sql.functions._

/**
  * Created by wallace on 2018/11/16.
  */

case class Cookie(cookieid: String, createtime: String, pv: Int)

object WindowExprDemo extends CreateSparkSession {

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = createSparkSession("WindowExprDemo")

    import spark.implicits._
    val rdd: Dataset[String] = spark.read.format("csv").textFile("./demo/SparkDemo/src/main/resources/spark_sql_data")
    val df: DataFrame = rdd.map(_.split(",")).map(x => Cookie(x(0), x(1), x(2).toInt)).toDF
    df.show
    // +--------+----------+---+
    // |cookieid|createtime| pv|
    // +--------+----------+---+
    // | cookie1|2015-04-10|  1|
    // | cookie1|2015-04-11|  5|
    // | cookie1|2015-04-12|  7|
    // | cookie1|2015-04-13|  3|
    // | cookie1|2015-04-14|  2|
    // | cookie1|2015-04-15|  4|
    // | cookie1|2015-04-16|  4|
    // | cookie2|2015-04-10|  2|
    // | cookie2|2015-04-11|  3|
    // | cookie2|2015-04-12|  5|
    // | cookie2|2015-04-13|  6|
    // | cookie2|2015-04-14|  3|
    // | cookie2|2015-04-15|  9|
    // | cookie2|2015-04-16|  7|
    // +--------+----------+---+


    df.selectExpr("*",
      "NTILE(2) OVER(PARTITION BY cookieid ORDER BY createtime) AS rn1",
      "NTILE(3) OVER(PARTITION BY cookieid ORDER BY createtime) AS rn2",
      "NTILE(4) OVER(ORDER BY createtime) AS rn3").toJSON.show(200, truncate = false)
    // 18/11/15 23:39:48 WARN WindowExec: No Partition Defined for Window operation! Moving all data to a single partition, this can cause serious performance degradation.
    // +-------------------------------------------------------------------------------+
    // |value                                                                          |
    // +-------------------------------------------------------------------------------+
    // |{"cookieid":"cookie1","createtime":"2015-04-10","pv":1,"rn1":1,"rn2":1,"rn3":1}|
    // |{"cookieid":"cookie2","createtime":"2015-04-10","pv":2,"rn1":1,"rn2":1,"rn3":1}|
    // |{"cookieid":"cookie1","createtime":"2015-04-11","pv":5,"rn1":1,"rn2":1,"rn3":1}|
    // |{"cookieid":"cookie2","createtime":"2015-04-11","pv":3,"rn1":1,"rn2":1,"rn3":1}|
    // |{"cookieid":"cookie1","createtime":"2015-04-12","pv":7,"rn1":1,"rn2":1,"rn3":2}|
    // |{"cookieid":"cookie2","createtime":"2015-04-12","pv":5,"rn1":1,"rn2":1,"rn3":2}|
    // |{"cookieid":"cookie1","createtime":"2015-04-13","pv":3,"rn1":1,"rn2":2,"rn3":2}|
    // |{"cookieid":"cookie2","createtime":"2015-04-13","pv":6,"rn1":1,"rn2":2,"rn3":2}|
    // |{"cookieid":"cookie1","createtime":"2015-04-14","pv":2,"rn1":2,"rn2":2,"rn3":3}|
    // |{"cookieid":"cookie2","createtime":"2015-04-14","pv":3,"rn1":2,"rn2":2,"rn3":3}|
    // |{"cookieid":"cookie1","createtime":"2015-04-15","pv":4,"rn1":2,"rn2":3,"rn3":3}|
    // |{"cookieid":"cookie2","createtime":"2015-04-15","pv":9,"rn1":2,"rn2":3,"rn3":4}|
    // |{"cookieid":"cookie1","createtime":"2015-04-16","pv":4,"rn1":2,"rn2":3,"rn3":4}|
    // |{"cookieid":"cookie2","createtime":"2015-04-16","pv":7,"rn1":2,"rn2":3,"rn3":4}|
    // +-------------------------------------------------------------------------------+

    val we: WindowSpec = Window.partitionBy("cookieid").orderBy("createtime")
    df.select('*,
      ntile(2) over we as "rn1",
      ntile(3) over we as "rn2",
      ntile(4) over we as "rn3").toJSON.show(200, truncate = false)
    // +-------------------------------------------------------------------------------+
    // |value                                                                          |
    // +-------------------------------------------------------------------------------+
    // |{"cookieid":"cookie1","createtime":"2015-04-10","pv":1,"rn1":1,"rn2":1,"rn3":1}|
    // |{"cookieid":"cookie1","createtime":"2015-04-11","pv":5,"rn1":1,"rn2":1,"rn3":1}|
    // |{"cookieid":"cookie1","createtime":"2015-04-12","pv":7,"rn1":1,"rn2":1,"rn3":2}|
    // |{"cookieid":"cookie1","createtime":"2015-04-13","pv":3,"rn1":1,"rn2":2,"rn3":2}|
    // |{"cookieid":"cookie1","createtime":"2015-04-14","pv":2,"rn1":2,"rn2":2,"rn3":3}|
    // |{"cookieid":"cookie1","createtime":"2015-04-15","pv":4,"rn1":2,"rn2":3,"rn3":3}|
    // |{"cookieid":"cookie1","createtime":"2015-04-16","pv":4,"rn1":2,"rn2":3,"rn3":4}|
    // |{"cookieid":"cookie2","createtime":"2015-04-10","pv":2,"rn1":1,"rn2":1,"rn3":1}|
    // |{"cookieid":"cookie2","createtime":"2015-04-11","pv":3,"rn1":1,"rn2":1,"rn3":1}|
    // |{"cookieid":"cookie2","createtime":"2015-04-12","pv":5,"rn1":1,"rn2":1,"rn3":2}|
    // |{"cookieid":"cookie2","createtime":"2015-04-13","pv":6,"rn1":1,"rn2":2,"rn3":2}|
    // |{"cookieid":"cookie2","createtime":"2015-04-14","pv":3,"rn1":2,"rn2":2,"rn3":3}|
    // |{"cookieid":"cookie2","createtime":"2015-04-15","pv":9,"rn1":2,"rn2":3,"rn3":3}|
    // |{"cookieid":"cookie2","createtime":"2015-04-16","pv":7,"rn1":2,"rn2":3,"rn3":4}|
    // +-------------------------------------------------------------------------------+
  }


}

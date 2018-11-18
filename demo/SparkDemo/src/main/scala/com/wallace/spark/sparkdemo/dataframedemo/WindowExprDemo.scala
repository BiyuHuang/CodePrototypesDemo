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
    spark.conf.set("spark.sql.shuffle.partitions", "10")
    spark.conf.set("spark.default.parallelism", "10")

    import spark.implicits._
    val rdd: Dataset[String] = spark.read.format("csv").textFile("./demo/SparkDemo/src/main/resources/spark_sql_data")
    val df: DataFrame = rdd.map(_.split(",")).map(x => Cookie(x(0), x(1), x(2).toInt)).toDF
    df.show(100, truncate = false)

    df.selectExpr("*",
      "NTILE(2) OVER(PARTITION BY cookieid ORDER BY createtime) AS rn1",
      "NTILE(3) OVER(PARTITION BY cookieid ORDER BY createtime) AS rn2",
      "NTILE(4) OVER(ORDER BY createtime) AS rn3").toJSON.show(200, truncate = false)
    // WARN WindowExec: No Partition Defined for Window operation!
    // Moving all data to a single partition, this can cause serious performance degradation.

    val we: WindowSpec = Window.partitionBy("cookieid").orderBy("createtime")
    df.select('*,
      ntile(2) over we as 'rn1,
      ntile(3) over we as 'rn2,
      ntile(4) over we as 'rn3,
      row_number() over we as 'rn4,
      rank() over we as 'rn5,
      lag('createtime, 1, "1970-01-01") over we as 'last_1_time,
      lag('createtime, 2) over we as 'last_2_time,
      lead('createtime, 1, "1970-01-01") over we as 'lead_1_time,
      lead('createtime, 2) over we as 'lead_2_time
    )
      //.toJSON
      .show(200, truncate = false)

    spark.stop()
  }


}

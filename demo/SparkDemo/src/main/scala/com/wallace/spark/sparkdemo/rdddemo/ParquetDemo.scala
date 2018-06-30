/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.spark.sparkdemo.rdddemo

import com.wallace.common.Using
import com.wallace.spark.CreateSparkSession
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SaveMode, SparkSession}

/**
  * Created by wallace on 2018/3/14.
  */
object ParquetDemo extends CreateSparkSession with Using {
  private val spark: SparkSession = createSparkSession("ParquetDemo")
  private val sqlContext = spark.sqlContext

  /**
    * @param file parquet file
    * @return
    */
  def readParquetFile(file: String): DataFrame = {
    sqlContext.read.parquet(file)
  }

  def writeRddToParquetFile(rdd: RDD[Row], beanClass: Class[_], dest: String): Unit = {
    val df: DataFrame = sqlContext.createDataFrame(rdd, beanClass.getClass)
    df.write.mode(SaveMode.Append).parquet(dest)
  }

  case class DataSchema(id: BigInt, time: String, value: String)

}

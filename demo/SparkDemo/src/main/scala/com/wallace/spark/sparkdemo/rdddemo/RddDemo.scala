package com.wallace.spark.sparkdemo.rdddemo

import com.wallace.common.Using
import com.wallace.spark.CreateSparkSession
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
  * com.wallace.spark.sparkdemo.rdddemo
  * Created by 10192057 on 2017/12/19 0019.
  */
object RddDemo extends CreateSparkSession with Using {
  private val _spark: SparkSession = createSparkSession("RddDemo")
  val path: String = "./demo/SparkDemo/src/main/resources/trainingData.csv"
  val minPartitions: Int = Math.min(Runtime.getRuntime.availableProcessors(), 10)

  def readTextFile(filePath: String): Unit = {
    val sc: SparkContext = _spark.sparkContext
    log.warn(s"UI_Web: ${sc.uiWebUrl.getOrElse("")}")
    val fileRdd: RDD[String] = sc.textFile(filePath, minPartitions)
    log.warn(s"Count: ${fileRdd.count()}")
    fileRdd.map(x => (x, 1)).reduceByKey(_ + _)
  }
}

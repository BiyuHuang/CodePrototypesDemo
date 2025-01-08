package com.wallace.common

import org.apache.spark.sql.SparkSession

/**
  * Created by Wallace on 2016/11/10.
  */
trait CreateSparkSession extends FuncRunDuration with LogSupport {
  def createSparkSession(appName: String, master: String = "local[*]"): SparkSession = {
    val warehouseLocation = System.getProperty("user.dir").replaceAll("\\\\", "/") + "/" + "spark-warehouse"
    val spark: SparkSession = SparkSession
      .builder()
      .master(master)
      .appName(appName)
      .config("spark.sql.warehouse.dir", warehouseLocation)
      .config("xgboost.spark.debug", "true")
      //.enableHiveSupport()
      .getOrCreate()

    spark
  }
}

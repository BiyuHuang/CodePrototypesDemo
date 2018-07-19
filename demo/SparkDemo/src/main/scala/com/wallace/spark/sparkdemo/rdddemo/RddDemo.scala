package com.wallace.spark.sparkdemo.rdddemo

import com.wallace.common.{CreateSparkSession, Using}
import org.apache.hadoop.conf.Configuration
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession


/**
  * com.wallace.spark.sparkdemo.rdddemo
  * Created by 10192057 on 2017/12/19 0019.
  */
object RddDemo extends CreateSparkSession with Using {
  private val _spark: SparkSession = createSparkSession("RddDemo")
  val path: String = "./demo/SparkDemo/src/main/resources/trainingData.csv.gz"
  val minPartitions: Int = Math.min(Runtime.getRuntime.availableProcessors(), 10)

  def readTextFile(filePath: String): Unit = {
    val sc: SparkContext = _spark.sparkContext
    val hadoopConf = new Configuration()
    hadoopConf.set("mapreduce.input.fileinputformat.split.maxsize", "134217728")
    hadoopConf.set("mapreduce.input.fileinputformat.split.minsize", "0")
    hadoopConf.set("mapreduce.input.fileinputformat.split.minsize.per.node", "0")
    hadoopConf.set("mapreduce.input.fileinputformat.split.minsize.per.rack", "134217728")
    sc.setLogLevel("WARN")
    log.warn(s"UI_Web: ${sc.uiWebUrl.getOrElse("")}")
    val fileRdd: RDD[String] = sc.textFile(filePath, minPartitions)
    val data: RDD[String] = sc.newAPIHadoopFile(filePath,
      classOf[org.apache.hadoop.mapreduce.lib.input.CombineTextInputFormat],
      classOf[org.apache.hadoop.io.LongWritable],
      classOf[org.apache.hadoop.io.Text],
      hadoopConf).map(_._2.toString)

    log.warn(s"Count1: ${data.count()}")
    log.warn(s"Count2: ${fileRdd.count()}")
    log.info(s"Last String: ${data.collect().last}")
    fileRdd.map(x => (x, 1)).reduceByKey(_ + _)
  }
}

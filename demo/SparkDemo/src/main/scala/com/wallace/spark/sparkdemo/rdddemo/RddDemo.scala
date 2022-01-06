/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.spark.sparkdemo.rdddemo

import com.wallace.common.{CreateSparkSession, Using}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}


/**
 * com.wallace.spark.sparkdemo.rdddemo
 * Created by 10192057 on 2017/12/19 0019.
 */
object RddDemo extends CreateSparkSession with Using {
  private val _spark: SparkSession = createSparkSession("RddDemo")
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
    log.info("Last String: %s".format(data.take(1).head))
    fileRdd.map(x => (x, 1)).reduceByKey(_ + _)
    val tempRdd = fileRdd.repartition(10)
    tempRdd.mapPartitionsWithIndex {
      (index, iter) =>
        val len = index.toString.length
        iter.zipWithIndex.map {
          elem =>
            val k = s"${2018 % 9}0821164$index${s"%0${11 - len}d".format(elem._2)}".toLong
            (k, elem._1)
        }
    }.take(10).foreach(println)

    println(tempRdd.partitions.length)
  }

  def main(args: Array[String]): Unit = {
    val sc = _spark.sparkContext
    val hc = new HiveContext(sc)
    import hc.implicits._
    val fs: FileSystem = FileSystem.get(sc.hadoopConfiguration)
    println(s"Home Directory: ${fs.getHomeDirectory}")
    val rdd: RDD[String] = sc.parallelize(Array("hello world", "hello", "world", "hello world world"), 2)

    val res: RDD[(String, Int)] = rdd.flatMap(line => line.split("\\s+"))
      .map(y => (y, 1))
      .reduceByKey(_ + _)

    val df: DataFrame = res.toDF("id", "cnt")

    df.write.format("orc").mode(SaveMode.Overwrite).save("./temp/")

    res.collect.foreach(println)

  }
}

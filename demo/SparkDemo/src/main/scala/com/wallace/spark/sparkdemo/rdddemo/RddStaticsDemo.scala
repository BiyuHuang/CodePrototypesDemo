/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.spark.sparkdemo.rdddemo

import com.wallace.common.{CreateSparkSession, Using}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.util.StatCounter

/**
  * Created by wallace on 2019/5/30.
  */
object RddStaticsDemo extends CreateSparkSession with Using {
  private val _spark: SparkSession = createSparkSession("RddStaticsDemo")

  def main(args: Array[String]): Unit = {
    val sc = _spark.sparkContext
    val data: RDD[(String, (Double, Double))] = sc.parallelize(Array((1, 2, 3),
      (1, 4, 5), (4, 5, 6), (4, 7, 8), (7, 8, 9), (10, 11, 12),
      (10, 13, 14), (10, 1, 2), (1, 100, 100), (10, 11, 2), (10, 11, 2),
      (1, 2, 5), (4, 7, 6)), 3)
      .map(row => ("K" + row._1, (row._2.toDouble, row._3.toDouble)))
    log.info(data.take(3).mkString("|"))
    val dataStatSingleCol: RDD[(String, StatCounter)] = data
      .map(x => (x._1, x._2._1))
      .aggregateByKey(new StatCounter())((s, v) => s.merge(v), (s, t) => s.merge(t))


    val dataStatMultiCols: RDD[(String, Array[StatCounter])] = data
      .map(x => (x._1, Array(x._2._1, x._2._2)))
      .aggregateByKey(Array.fill(2)(new StatCounter()))(
        (s: Array[StatCounter], v: Array[Double]) => s.zip(v).map { case (si, vi) => si merge vi },
        (s: Array[StatCounter], t: Array[StatCounter]) => s.zip(t).map { case (si, ti) => si merge ti })


    dataStatSingleCol.collect.foreach(println)
    dataStatMultiCols.collect.map(x => x._1 + "\t=>\t" + x._2.map(_.toString()).mkString(" , ")).foreach(println)
  }
}

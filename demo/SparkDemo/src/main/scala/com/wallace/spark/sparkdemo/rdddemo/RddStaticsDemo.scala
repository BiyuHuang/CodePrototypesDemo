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
  * Calc mean/variance/max/min/count And there's no need to collect data by single column.
  * Demo Result:
  * K1	=>	(count: 4, mean: 27.000000, stdev: 42.154478, max: 100.000000, min: 2.000000) , (count: 4, mean: 28.250000, stdev: 41.432928, max: 100.000000, min: 3.000000)
  * K4	=>	(count: 3, mean: 6.333333, stdev: 0.942809, max: 7.000000, min: 5.000000) , (count: 3, mean: 6.666667, stdev: 0.942809, max: 8.000000, min: 6.000000)
  * K7	=>	(count: 1, mean: 8.000000, stdev: 0.000000, max: 8.000000, min: 8.000000) , (count: 1, mean: 9.000000, stdev: 0.000000, max: 9.000000, min: 9.000000)
  * K10	=>	(count: 5, mean: 9.400000, stdev: 4.270831, max: 13.000000, min: 1.000000) , (count: 5, mean: 6.400000, stdev: 5.425864, max: 14.000000, min: 2.000000)
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

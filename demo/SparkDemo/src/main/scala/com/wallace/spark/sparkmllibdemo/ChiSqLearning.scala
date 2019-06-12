/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.spark.sparkmllibdemo

import com.wallace.common.{CreateSparkSession, Using}
import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.mllib.stat.test.ChiSqTestResult

/**
  * Created by wallace on 2019/6/6.
  */
object ChiSqLearning extends CreateSparkSession with Using {
  def main(args: Array[String]): Unit = {
    usingSpark(createSparkSession("ChiSqLearning")) {
      spark =>
        val vd: linalg.Vector = Vectors.dense(1, 2, 2, 3, 3, 3, 4, 4, 5)
        val vdRes: ChiSqTestResult = Statistics.chiSqTest(vd)
        log.info(s"$vdRes")
    }
  }
}

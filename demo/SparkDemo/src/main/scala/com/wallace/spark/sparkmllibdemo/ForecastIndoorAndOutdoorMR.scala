/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.spark.sparkmllibdemo

import com.wallace.spark.CreateSparkSession
import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
  * Created by wallace on 2017/11/7.
  */
object ForecastIndoorAndOutdoorMR extends CreateSparkSession {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = createSparkSession("ForecastIndoorAndOutdoorMRDemo")
    spark.sql("use default")
    val rdd = spark.sparkContext.textFile("./demo/SparkDemo/src/main/resources/trainingData.csv")

    val splitRdds: Array[RDD[String]] = rdd.randomSplit(Array(0.7, 0.3), seed = 5L) //Split data into training (70%) and test(30%)
    val trainingData: RDD[LabeledPoint] = splitRdds.head.map(x => x.split(",")).map(x => LabeledPoint(
      x.last.toInt,
      Vectors.dense(Array(x(2).toDouble, x(3).toDouble,
        x(4).toDouble, x(5).toDouble, x(6).toDouble, x(7).toDouble))))

    log.info(s"Training Data Size: ${trainingData.count()}.")
    val testData: RDD[LabeledPoint] = splitRdds.last.map(x => x.split(",")).map(x => LabeledPoint(
      x.last.toInt,
      Vectors.dense(Array(x(2).toDouble, x(3).toDouble,
        x(4).toDouble, x(5).toDouble, x(6).toDouble, x(7).toDouble))))

    log.info(s"Test Data Size : ${testData.count()}.")

    // Run training algorithm to build the model
    val numIterations = 100
    val model = SVMWithSGD.train(trainingData, numIterations)
    // Clear the default threshold
    model.clearThreshold()
    //Compute raw scores on the testData
    val scoreAndLabels: RDD[(Double, Double)] = testData.map {
      point =>
        val score = model.predict(point.features)
        (score, point.label)
    }
    log.info(s"Compute raw scores on the testData")
    scoreAndLabels.foreach(x => log.info(s"score: ${x._1},label: ${x._2}."))
    /**
      * Get evaluation metrics.得到评估指标
      * 以召回率为y轴，以特异性为x轴，我们就直接得到了RoC曲线
      * 召回率越高，特异性越小，RoC曲线越靠近左上角，模型和算法就越高效
      * 另一方面，如果ROC是光滑的，那么基本可以判断没有太大的过拟合（overfitting)
      * 从几何的角度讲，RoC曲线下方的面积越大越大，则模型越优。
      * 所以有时候我们用RoC曲线下的面积，即AUC（Area Under Curve）值来作为算法和模型好坏的标准
      **/
    val metrics = new BinaryClassificationMetrics(scoreAndLabels)
    val auROC = metrics.areaUnderROC()

    log.info("Area under ROC = " + auROC)

  }


  private case class DataFields(reportCellKey: String, strongestNBPci: Int, aoa: Int, ta_calc: Double, rsrp: Double, rsrq: Double,
                                ta: Int, taDltValue: Int, mrtime: String, imsi: String, ndsKey: String, ueRecordID: String,
                                startTime: String, endTime: String, positionMarkReal: Int)

}

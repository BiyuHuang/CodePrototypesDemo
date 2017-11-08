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

import scala.util.Try

/**
  * Created by wallace on 2017/11/7.
  */
object ForecastIndoorAndOutdoorMR extends CreateSparkSession {
  protected val spark: SparkSession = createSparkSession("ForecastIndoorAndOutdoorMRDemo")

  def main(args: Array[String]): Unit = {
    val filePath: String = "./demo/SparkDemo/src/main/resources/trainingData.csv"
    val rdd: RDD[DataFields] = spark.sparkContext.textFile(filePath).filter(x => !x.toLowerCase.contains("positionmark_real")).map(_.split(",", -1)).map {
      x =>
        DataFields(x(0), Try(x(1).toDouble).getOrElse(Double.NaN), Try(x(2).toDouble).getOrElse(Double.NaN), Try(x(3).toDouble).getOrElse(Double.NaN),
          Try(x(4).toDouble).getOrElse(Double.NaN), Try(x(5).toDouble).getOrElse(Double.NaN), Try(x(6).toDouble).getOrElse(Double.NaN), Try(x(7).toDouble).getOrElse(Double.NaN), x(8), x(9), x(10), x(11), x(12), x(13), x(14).toInt)
    }

    val inputRDD: RDD[LabeledPoint] = rdd.map(dataFields => LabeledPoint(
      dataFields.positionMarkReal,
      Vectors.dense(Array(
        dataFields.strongestNBPci,
        dataFields.aoa,
        dataFields.ta_calc,
        dataFields.rsrp,
        dataFields.rsrq,
        dataFields.ta,
        dataFields.taDltValue))
    )).cache()
    val splitRdds: Array[RDD[LabeledPoint]] = inputRDD.randomSplit(Array(0.7, 0.3), seed = 5L) //Split data into training (70%) and test(30%)

    svmWithSGDModel(splitRdds, 200, 0.6)
    svmWithSGDModel(splitRdds, 200, 0.6, 0.05)
    svmWithSGDModel(splitRdds, 200, 0.6, 0.10)

  }

  /**
    * @param data              RDD of (label, array of features) pairs.
    * @param numIterations     Number of iterations of gradient descent to run.
    * @param stepSize          Step size to be used for each iteration of gradient descent.
    * @param regParam          Regularization parameter.
    * @param miniBatchFraction Fraction of data to be used per iteration.
    **/
  protected def svmWithSGDModel(data: Array[RDD[LabeledPoint]],
                                numIterations: Int,
                                stepSize: Double = 1.0,
                                regParam: Double = 0.01,
                                miniBatchFraction: Double = 1.0): Unit = {
    val trainingData: RDD[LabeledPoint] = data.head
    log.info(s"Training Data Size: ${trainingData.count()}.")
    val testData: RDD[LabeledPoint] = data.last
    log.info(s"Test Data Size : ${testData.count()}.")
    // Run training algorithm to build the model
    val model = SVMWithSGD.train(trainingData, numIterations, stepSize, regParam, miniBatchFraction)
    // Clear the default threshold
    model.clearThreshold()
    //Compute raw scores on the testData
    val scoreAndLabels: RDD[(Double, Double)] = testData.map {
      point =>
        val score = model.predict(point.features)
        (score, point.label)
    }
    log.info(s"Compute raw scores on the testData")
    //scoreAndLabels.foreach(x => log.info(s"score: ${x._1},label: ${x._2}."))
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
    log.warn(s"[Area under ROC = $auROC, numIterations = $numIterations, stepSize = $stepSize, regParam = $regParam, miniBatchFraction = $miniBatchFraction]")
  }

  private case class DataFields(reportCellKey: String, strongestNBPci: Double, aoa: Double, ta_calc: Double, rsrp: Double, rsrq: Double, ta: Double, taDltValue: Double, mrtime: String, imsi: String, ndsKey: String, ueRecordID: String, startTime: String, endTime: String, positionMarkReal: Int)

}

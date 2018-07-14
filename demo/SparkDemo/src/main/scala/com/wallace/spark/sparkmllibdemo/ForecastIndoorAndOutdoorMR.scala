/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.spark.sparkmllibdemo

import com.wallace.common.CreateSparkSession
import org.apache.spark.mllib.classification.{LogisticRegressionWithLBFGS, NaiveBayes, SVMWithSGD}
import org.apache.spark.mllib.evaluation.{BinaryClassificationMetrics, MulticlassMetrics}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{LabeledPoint, LinearRegressionWithSGD}
import org.apache.spark.mllib.tree.RandomForest
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
    val splitRdds: Array[RDD[LabeledPoint]] = inputRDD.randomSplit(Array(0.7, 0.3), seed = 1L) //Split data into training (70%) and test(30%)
    svmWithSGDModel(splitRdds, 200, 0.6, 0.0005)
    //    linearRegressionWithSGD(splitRdds)
    //logisticRegressionWithLBFGSModel(splitRdds)
    //    naiveBayes(splitRdds)

    // randomForestModel(splitRdds)

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
    //model.clearThreshold()
    //Compute raw scores on the testData
    val predictionAndLabels: RDD[(Double, Double)] = testData.map {
      point =>
        val score = model.predict(point.features)
        (score, point.label)
    }
    log.info(s"Compute raw scores on the testData")
    predictionAndLabels.foreach(x => log.info(s"score: ${x._1},label: ${x._2}."))
    /**
      * Get evaluation metrics.得到评估指标
      * 以召回率为y轴，以特异性为x轴，我们就直接得到了RoC曲线
      * 召回率越高，特异性越小，RoC曲线越靠近左上角，模型和算法就越高效
      * 另一方面，如果ROC是光滑的，那么基本可以判断没有太大的过拟合（overfitting)
      * 从几何的角度讲，RoC曲线下方的面积越大越大，则模型越优。
      * 所以有时候我们用RoC曲线下的面积，即AUC（Area Under Curve）值来作为算法和模型好坏的标准
      **/
    val metrics = new BinaryClassificationMetrics(predictionAndLabels)
    val auROC = metrics.areaUnderROC()
    log.warn(s"[Model = SVMWithSGDModel, Area under ROC = $auROC, numIterations = $numIterations, stepSize = $stepSize, regParam = $regParam, miniBatchFraction = $miniBatchFraction]")
  }

  protected def logisticRegressionWithLBFGSModel(data: Array[RDD[LabeledPoint]]): Unit = {
    val trainingData = data.head
    val testData = data.last

    val model = new LogisticRegressionWithLBFGS()
      .setNumClasses(20)
      .run(trainingData)

    val predictionAndLabels = testData.map {
      point =>
        val prediction = model.predict(point.features)
        (prediction, point.label)
    }

    // Get evaluation metrics.获取评估指标
    val metrics = new MulticlassMetrics(predictionAndLabels)
    val precision: Double = metrics.accuracy

    log.warn(s"[Model = LogisticRegressionWithLBFGSModel, Precision = $precision]")
  }

  /**
    * RandomForest
    **/
  protected def randomForestModel(data: Array[RDD[LabeledPoint]]): Unit = {
    val trainingData = data.head
    val testData = data.last
    // Train a RandomForest model.
    // Empty categoricalFeaturesInfo indicates all features are continuous.
    val numClasses = 2
    val categoricalFeaturesInfo = Map[Int, Int]()
    val numTrees = 3 // Use more in practice.
    val featureSubsetStrategy = "auto" // Let the algorithm choose.
    val impurity = "variance"
    val maxDepth = 4
    val maxBins = 32

    val model = RandomForest.trainRegressor(trainingData, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

    // Evaluate model on test instances and compute test error
    val predictionAndLabels: RDD[(Double, Double)] = testData.map { point =>
      val prediction = model.predict(point.features)
      log.warn(s"Label: ${point.label}, Prediction: $prediction")
      (point.label, prediction)
    }

    val testMSE = predictionAndLabels.map { case (v, p) => math.pow(v - p, 2) }.mean()
    val accuracy = 1.0 * predictionAndLabels.filter(x => Math.abs(x._1 - x._2) < 0.2).count() / testData.count()
    log.warn(s"[Model = RandomForestModel, Test Mean Squared Error = $testMSE, Precision = $accuracy]")
    log.warn("Learned regression forest model:\n" + model.toDebugString)
  }

  protected def linearRegressionWithSGDModel(data: Array[RDD[LabeledPoint]]): Unit = {
    val trainingData = data.head
    val testData = data.last

    val model = LinearRegressionWithSGD.train(trainingData, 500, 0.0000000000000001)
    val predictionAndLabels = testData.map {
      point =>
        val prediction = model.predict(point.features)
        (prediction, point.label)
    }

    // Get evaluation metrics.获取评估指标
    val mse = predictionAndLabels.map { case (v, p) => math.pow(v - p, 2) }.mean()
    log.warn(s"[Model = LinearRegressionWithSGDModel, Training Mean Squared Error = $mse]")
  }

  /**
    * 朴素贝叶斯
    **/
  protected def naiveBayesModel(data: Array[RDD[LabeledPoint]], lambda: Double = 1.0, modelType: String = "bernoulli"): Unit = {
    val trainingData = data.head
    val testData = data.last

    val model = NaiveBayes.train(trainingData, lambda, modelType)
    val predictionAndLabels = testData.map {
      point =>
        val prediction = model.predict(point.features)
        (prediction, point.label)
    }

    // Get evaluation metrics.获取评估指标
    val accuracy = 1.0 * predictionAndLabels.filter(x => x._1 == x._2).count() / testData.count()
    log.warn(s"[Model = NaiveBayesModel, Precision = $accuracy]")
  }

  private case class DataFields(reportCellKey: String, strongestNBPci: Double, aoa: Double, ta_calc: Double, rsrp: Double, rsrq: Double, ta: Double, taDltValue: Double, mrtime: String, imsi: String, ndsKey: String, ueRecordID: String, startTime: String, endTime: String, positionMarkReal: Int)

}

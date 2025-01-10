package com.wallace.spark.sparkmllibdemo

import com.wallace.common.{CreateSparkSession, Using}
import ml.dmlc.xgboost4j.scala.spark.{XGBoostClassificationModel, XGBoostClassifier}
import org.apache.spark.ml.evaluation.{BinaryClassificationEvaluator, MulticlassClassificationEvaluator}
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.sql.functions._

import scala.collection.immutable
import scala.util.Random

/**
 * Author: biyu.huang
 * Date: 2024/12/31 11:39
 * Description:
 */
object XGBoostDemo extends CreateSparkSession with Using {
  def main(args: Array[String]): Unit = {
    usingSpark(createSparkSession("XGBoost Demo")) {
      spark =>
        // Randomly generate 20,000 samples
        val random = new Random()
        val data = (1 to 20000).map { _ =>
          val label = random.nextInt(2).toDouble // 随机生成 0 或 1
          val feature1 = random.nextDouble() * 10 + 1.0 // 特征 1，范围 [0, 10)
          val feature2 = random.nextDouble() * 20 + 1.0 // 特征 2，范围 [0, 20)
          val feature3 = random.nextDouble() * 30 + 1.0 // 特征 3，范围 [0, 30)
          (label, feature1, feature2, feature3)
        }.zipWithIndex.map {
          case ((label, feature1, feature2, feature3), id) =>
            (label, feature1, feature2, feature3, id)
        }

        val df = spark.createDataFrame(data)
          .toDF("label", "feature1", "feature2", "feature3", "id")
        // Process features
        val assembler = new VectorAssembler()
          .setInputCols(Array("feature1", "feature2", "feature3"))
          .setOutputCol("features")
        val transformedDF = assembler.transform(df)
        transformedDF.show()
        // Split raw_data into train_data and test_data
        val Array(trainData, testData) = transformedDF.randomSplit(Array(0.8, 0.2), seed = 42)

        // Split train_data into k-folds
        val k = 5 // Define K fold
        val folds: Array[Dataset[Row]] = trainData.randomSplit(Array.fill(k)(1.0 / k), seed = 42)
        // Verify the data distribution across folds.
        folds.zipWithIndex.foreach { case (df, idx) => log.info(s"Fold $idx: ${df.count()} rows") }

        // XGBoostClassifier parameters
        val params = Map(
          "seed" -> 42,
          "eta" -> 0.1,
          "max_depth" -> 4,
          "objective" -> "binary:logistic", // 二分类 multi:softprob
          "num_round" -> 200, // 迭代次数
          "num_workers" -> 2, // 并行任务数
          "verbosity" -> 2, // 输出详细日志
          "handle_invalid" -> "keep",
          "use_external_memory" -> "false" // 避免使用 Rabit Tracker
        )

        // model and logloss
        val results: immutable.Seq[(Double, XGBoostClassificationModel, Map[String, Double])] =
          (0 until k).map { i =>
            val trainData = folds.filterNot(_ == folds(i)).reduce(_.union(_)) // 除第 i 折外的所有数据
            val testData = folds(i) // 第 i 折作为验证集

            // 初始化 XGBoostClassifier
            val xgbClassifier = new XGBoostClassifier(params)
              .setFeaturesCol("features")
              .setLabelCol("label")
              .setEvalSets(Map("train" -> trainData.toDF(), "eval" -> testData.toDF())) // 指定训练和验证集
            // Model training
            val model = xgbClassifier.fit(trainData)
            log.info(s"Model summary: ${model.summary.toString()}")
            // Model transform
            val predictions = model.transform(testData)
            // Calculate log-loss
            val epsilon: Double = 1e-15
            val logLossUDF = udf((y_true: Double, y_pred: Double) => {
              val clipped = Math.max(epsilon, Math.min(1 - epsilon, y_pred))
              -y_true * Math.log(clipped) - (1 - y_true) * Math.log(1 - clipped)
            })

            predictions.show(10, truncate = false)
            val vectorToArray = udf((v: org.apache.spark.ml.linalg.Vector) => v.toArray)
            val logLossDF = predictions
              .withColumn("probability", vectorToArray(col("probability")))
              .select(
                col("label").alias("y_true"),
                col("probability").getItem(1).alias("y_pred")
              ).withColumn("logloss", logLossUDF(col("y_true"), col("y_pred")))
            val avgLogLoss = logLossDF.select(avg("logloss")).collect()(0)(0)
            val metrics: Map[String, Double] = modelEvaluation(predictions)
            log.info(s"Fold $i Logloss: $avgLogLoss, AUC-ROC: ${metrics("auc-roc")}, " +
              s"Accuracy: ${metrics("accuracy")}, Precision: ${metrics("precision")}, " +
              s"Recall: ${metrics("recall")}, F1 Score: ${metrics("f1_score")}")
            // Confusion Matrix
            val confusionMatrix = predictions
              .groupBy("label", "prediction")
              .count().orderBy("label", "prediction")
            confusionMatrix.show()
            (avgLogLoss.asInstanceOf[Double], model, metrics)
          }

        // Predict 投票法 (Voting)
        val predictions: DataFrame = results.map(_._2.transform(testData))
          .map(_.select("id", "label", "features", "rawPrediction", "prediction"))
          .reduce(_.union(_))
          .groupBy("id")
          .agg(
            expr("mode(prediction)").alias("prediction"),
            max("rawPrediction").alias("rawPrediction"),
            max("label").alias("label"),
            max("features").alias("features")
          )
        //          results.minBy(_._1)._2.transform(testData)
        predictions.select("id", "label", "features", "rawPrediction", "prediction").show()

        // AUC
        val aucArray =  results.map(_._3.apply("auc-roc"))
        val auc = aucArray.sum / aucArray.size
        val metrics = modelEvaluation(predictions)
        log.info(s"AUC-ROC: $auc, " +
          s"Accuracy: ${metrics("accuracy")}, Precision: ${metrics("precision")}, " +
          s"Recall: ${metrics("recall")}, F1 Score: ${metrics("f1_score")}")
        // 创建混淆矩阵
        val confusionMatrix =
          predictions.groupBy("label", "prediction").count().orderBy("label", "prediction")
        confusionMatrix.show()
    }
  }

  private def modelEvaluation(prediction: DataFrame,
    objective: String = "binary"): Map[String, Double] = {
    val auc: Double = objective match {
      case "binary" =>
        val binaryEvaluator = new BinaryClassificationEvaluator()
          .setLabelCol("label")
          .setRawPredictionCol("rawPrediction")
          .setMetricName("areaUnderROC")
        // AUC
        binaryEvaluator.evaluate(prediction)
      case "multi" =>
        Double.NaN
    }
    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
    // Accuracy
    val accuracy: Double = evaluator.setMetricName("accuracy").evaluate(prediction)
    // Precision
    val precision: Double = evaluator.setMetricName("precisionByLabel").evaluate(prediction)
    // Recall
    val recall: Double = evaluator.setMetricName("recallByLabel").evaluate(prediction)
    // F1 score
    val f1Score: Double = evaluator.setMetricName("f1").evaluate(prediction)

    Map(
      "auc-roc" -> auc,
      "accuracy" -> accuracy,
      "precision" -> precision,
      "recall" -> recall,
      "f1_score" -> f1Score
    )
  }
}

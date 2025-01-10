package com.wallace.spark.sparkmllibdemo

import com.wallace.common.{CreateSparkSession, Using}
import ml.dmlc.xgboost4j.scala.spark.XGBoostClassifier
import ml.dmlc.xgboost4j.scala.spark.TaskConf
import org.apache.spark.ml.evaluation.{BinaryClassificationEvaluator, MulticlassClassificationEvaluator}
import org.apache.spark.ml.feature.VectorAssembler

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
        // 随机生成 10,000 个样本
        val random = new Random()
        val data = (1 to 20000).map { _ =>
          val label = random.nextInt(2).toDouble // 随机生成 0 或 1
          val feature1 = random.nextDouble() * 10 + 1.0 // 特征 1，范围 [0, 10)
          val feature2 = random.nextDouble() * 20 + 1.0 // 特征 2，范围 [0, 20)
          val feature3 = random.nextDouble() * 30 + 1.0 // 特征 3，范围 [0, 30)
          (label, feature1, feature2, feature3)
        }

        val df = spark.createDataFrame(data).toDF("label", "feature1", "feature2", "feature3")

        // 特征列组合
        val assembler = new VectorAssembler().setInputCols(Array("feature1", "feature2", "feature3")).setOutputCol("features")

        val transformedDF = assembler.transform(df)
        transformedDF.show()
        // XGBoostClassifier 参数
        val trackerConf = new TrackerConf(workerConnectionTimeout = 30000L, // 超时时间
          trackerImpl = "scala") // 使用 Scala Tracker
        val params = Map(
          "eta" -> 0.1,
          "max_depth" -> 5,
          "objective" -> "binary:logistic", // 二分类
          "num_round" -> 20, // 迭代次数
          "num_workers" -> 2, // 并行任务数
          "verbosity" -> 2, // 输出详细日志
          "handle_invalid" -> "keep",
          "use_external_memory" -> "false", // 避免使用 Rabit Tracker
          "tracker_conf" -> trackerConf
        )

        // 初始化 XGBoostClassifier
        val xgbClassifier = new XGBoostClassifier(params).setFeaturesCol("features").setLabelCol("label")

        // 训练模型
        val xgbModel = xgbClassifier.fit(transformedDF)

        // 打印模型摘要
        println("Model summary:")
        println(xgbModel)

        // 使用模型进行预测
        val predictions = xgbModel.transform(transformedDF)

        // 打印预测结果
        predictions.select("label", "features", "prediction").show()

        // 二分类 AUC
        val binaryEvaluator = new BinaryClassificationEvaluator().setLabelCol("label").setRawPredictionCol("rawPrediction").setMetricName("areaUnderROC")

        val auc = binaryEvaluator.evaluate(predictions)
        println(s"AUC-ROC: $auc")

        val evaluator = new MulticlassClassificationEvaluator().setLabelCol("label").setPredictionCol("prediction")

        // Accuracy
        val accuracy = evaluator.setMetricName("accuracy").evaluate(predictions)
        println(s"Accuracy: $accuracy")

        // Precision
        val precision = evaluator.setMetricName("precisionByLabel").evaluate(predictions)
        println(s"Precision: $precision")

        // Recall
        val recall = evaluator.setMetricName("recallByLabel").evaluate(predictions)
        println(s"Recall: $recall")

        // 多分类的 F1 分数
        val f1Score = evaluator.setMetricName("f1").evaluate(predictions)
        println(s"F1 Score: $f1Score")

        // 创建混淆矩阵
        val confusionMatrix = predictions.groupBy("label", "prediction").count().orderBy("label", "prediction")
        confusionMatrix.show()
    }
  }
}

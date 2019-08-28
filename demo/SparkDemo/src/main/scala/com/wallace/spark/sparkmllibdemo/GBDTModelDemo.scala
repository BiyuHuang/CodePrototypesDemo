package com.wallace.spark.sparkmllibdemo

import com.github.fommil.netlib.BLAS
import com.wallace.common.{CreateSparkSession, Using}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.GradientBoostedTrees
import org.apache.spark.mllib.tree.configuration.BoostingStrategy
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel
import org.apache.spark.rdd.RDD

/**
  * Created by wallace on 2019/8/6.
  */
object GBDTModelDemo extends CreateSparkSession with Using {
  def main(args: Array[String]): Unit = {
    usingSpark(createSparkSession("LightGBM_Demo")) {
      spark =>
        spark.sparkContext.setLogLevel("WARN")
        val trainRdd: RDD[LabeledPoint] = spark.sparkContext.parallelize(Array(
          LabeledPoint(1.0, Vectors.dense(Array(0.324, 0.546, 0.235, 0.0))),
          LabeledPoint(0.0, Vectors.dense(Array(0.414, 0.346, 0.135, 1.0))),
          LabeledPoint(1.0, Vectors.dense(Array(0.234, 0.446, 0.035, 1.0))),
          LabeledPoint(0.0, Vectors.dense(Array(0.3124, 0.466, 0.135, 0.0))),
          LabeledPoint(1.0, Vectors.dense(Array(1.324, 3.546, 1.235, 1.0)))))

        val bStrategy: BoostingStrategy = BoostingStrategy.defaultParams("Classification")
        bStrategy.setNumIterations(30)
        bStrategy.treeStrategy.setNumClasses(2)
        bStrategy.treeStrategy.setMaxDepth(4)
        bStrategy.treeStrategy.setMaxBins(10)
        bStrategy.treeStrategy.setCheckpointInterval(20)
        val gbdtModel: GradientBoostedTreesModel = GradientBoostedTrees.train(trainRdd, bStrategy)

        log.info(gbdtModel.toDebugString)

        trainRdd.foreach {
          point =>
            val pScore: Double = {
              val treePredictions: Array[Double] = gbdtModel.trees.map(_.predict(point.features))
              val prediction = BLAS.getInstance().ddot(gbdtModel.numTrees, treePredictions, 1, gbdtModel.treeWeights, 1)
              1.0 / (1.0 + math.exp(-prediction))
            }

            log.info(s"Label: ${point.label}, Prediction: $pScore.")
        }
    }
  }
}
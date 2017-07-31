package com.wallace.spark.sparkmllibdemo

import com.wallace.common.FuncRuntimeDur
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.sql.{Row, SparkSession}

/**
  * Created by Wallace on 2016/11/1.
  */
object PipelinesDemo extends FuncRuntimeDur {
  /** Create a LogisticRegression instance. This instance is an Estimator.
    * 逻辑回归函数实例
    * We may set parameters using setter methods.
    * MaxIter: 最大迭代次数. RegParam: 规范化系数
    * */
  protected val lr: LogisticRegression = new LogisticRegression().setMaxIter(10)
    .setRegParam(0.01)

  def main(args: Array[String]): Unit = {
    val warehouseLocation = System.getProperty("user.dir") + "/" + "spark-warehouse"
    val spark: SparkSession = SparkSession
      .builder()
      .master("local[*]")
      .appName("RddConvertToDataFrame")
      .config("spark.sql.warehouse.dir", warehouseLocation)
      .getOrCreate()
    val sc = spark.sparkContext

    MLDemo1(spark, lr)

    PipeLineFunc(spark, lr)
  }

  /**
    * Demo
    **/

  def MLDemo1(spark: SparkSession, lr: LogisticRegression) = {
    // Prepare training data from a list of (label, features) tuples.
    val training = spark.createDataFrame(Seq(
      (1.0, Vectors.dense(0.0, 1.1, 0.1)),
      (0.0, Vectors.dense(2.0, 1.0, -1.0)),
      (0.0, Vectors.dense(2.0, 1.3, 1.0)),
      (1.0, Vectors.dense(0.0, 1.2, -0.5))
    )).toDF("label", "features")

    training.show()


    // Print out the parameters, documentation, and any default values.
    println("[###PipelinesDemo###] LogisticRegression parameters:\n" + lr.explainParams() + "\n")


    // Learn a LogisticRegression model. This uses the parameters stored in lr.
    val model1 = lr.fit(training)

    // Since model1 is a Model (i.e., a Transformer produced by an Estimator),
    // we can view the parameters it used during fit().
    // This prints the parameter (name: value) pairs, where names are unique IDs for this
    // LogisticRegression instance.
    println("[###PipelinesDemo###] Model 1 was fit using parameters: " + model1.parent.extractParamMap)

    val paramMap = ParamMap(lr.maxIter -> 20)
      .put(lr.maxIter, 30) // Specify 1 Param. This overwrites the original maxIter.
      .put(lr.regParam -> 0.1, lr.threshold -> 0.55) // Specify multiple Params.

    // One can also combine ParamMaps.
    val paramMap2 = ParamMap(lr.probabilityCol -> "myProbability") // Change output column name.
    val paramMapCombined = paramMap ++ paramMap2

    // Now learn a new model using the paramMapCombined parameters.
    // paramMapCombined overrides all parameters set earlier via lr.set* methods.
    val model2 = lr.fit(training, paramMapCombined)
    println("Model 2 was fit using parameters: " + model2.parent.extractParamMap)

    // Prepare test data.
    val test = spark.createDataFrame(Seq(
      (1.0, Vectors.dense(-1.0, 1.5, 1.3)),
      (0.0, Vectors.dense(3.0, 2.0, -0.1)),
      (1.0, Vectors.dense(0.0, 2.2, -1.5))
    )).toDF("label", "features")

    // Make predictions on test data using the Transformer.transform() method.
    // LogisticRegression.transform will only use the 'features' column.
    // Note that model2.transform() outputs a 'myProbability' column instead of the usual
    // 'probability' column since we renamed the lr.probabilityCol parameter previously.
    model2.transform(test)
      .select("features", "label", "myProbability", "prediction")
      .collect()
      .foreach { case Row(features: Vector, label: Double, prob: Vector, prediction: Double) =>
        println(s"[###PipelinesDemo###] ($features, $label) -> prob=$prob, prediction=$prediction")
      }
  }

  /** PipeLineFunc
    *
    * @param spark SparkSession
    * @param lr    LogisticRegression
    * @return Unit
    * */
  def PipeLineFunc(spark: SparkSession, lr: LogisticRegression): Unit = {
    val training = spark.createDataFrame(Seq(
      (0L, "a b c d e spark", 1.0),
      (1L, "b d", 0.0),
      (2L, "spark f g h", 1.0),
      (3L, "hadoop mapreduce", 0.0))).toDF("id", "text", "label")

    training.show()

    val tokenizer = new Tokenizer().setInputCol("text").setOutputCol("words")
    val hashingTF = new HashingTF().setNumFeatures(1000).setInputCol(tokenizer.getOutputCol).setOutputCol("features")
    val pipeline = new Pipeline().setStages(Array(tokenizer, hashingTF, lr))

    val model = pipeline.fit(training)
    //    model.write.overwrite().save("./demo/SparkDemo/data/spark-logistic-regression-model")
    //
    //    pipeline.write.overwrite().save("./demo/SparkDemo/data/unfit-lr-model")
    //
    //    val sameModel = PipelineModel.load("./demo/SparkDemo/data/spark-logistic-regression-model")

    val test = spark.createDataFrame(Seq(
      (4L, "spark i j k"),
      (5L, "l m n"),
      (6L, "spark f g h spark f g h spark f g h"),
      (7L, "hadoop apache"),
      (8L, "a b c d e F spark"),
      (9L," spark")
    )).toDF("id", "text")

    model.transform(test).select("id", "text", "probability", "prediction")
      .collect().foreach {
      case Row(id: Long, text: String, prob: Vector, prediction: Double) =>
        println(s"($id,$text) --> prob=$prob,prediction=$prediction")
    }
  }
}

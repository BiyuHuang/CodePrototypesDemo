package com.wallace.spark.MllibDemo

import com.wallace.spark.common.LogSupport
import org.apache.spark.ml.feature.{HashingTF, IDF, Tokenizer}
import org.apache.spark.{SparkConf, SparkContext}


/**
  * Created by Wallace on 2016/10/17.
  */
object MllibDemo extends App with LogSupport {

  val conf = new SparkConf()
//    .setMaster("local")
    .setAppName("RddConvertToDataFrame")
  val sc = new SparkContext(conf)
  val sqlContext = new org.apache.spark.sql.SQLContext(sc)
  val sentenceData = sqlContext.createDataFrame(Seq(
    (0, "Hi I heard about Spark"),
    (0, "I wish Java could use case classes"),
    (1, "Logistic regression models are neat")
  )).toDF("label", "sentence")

  val tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words")
  val wordsData = tokenizer.transform(sentenceData)
  val hashingTF = new HashingTF().setInputCol("words").setOutputCol("rawFeatures").setNumFeatures(20)
  val featurizedData = hashingTF.transform(wordsData)

  val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
  val idfModel = idf.fit(featurizedData)
  val rescaledData = idfModel.transform(featurizedData)
  rescaledData.select("features", "label").take(3).foreach(println)

  sc.stop()
}

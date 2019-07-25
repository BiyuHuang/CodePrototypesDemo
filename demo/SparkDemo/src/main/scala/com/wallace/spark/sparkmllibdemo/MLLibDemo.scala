/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.spark.sparkmllibdemo

import java.io._

import com.wallace.common.LogSupport
import org.apache.spark.ml.feature.{HashingTF, IDF, IDFModel, Tokenizer}
import org.apache.spark.sql.{DataFrame, SparkSession}


/**
  * Created by Wallace on 2016/10/17.
  *
  * TF-IDF（term frequency–inverse document frequency）:TF表示 词频,IDF表示 反文档频率.
  * TF-IDF主要内容就是: 如果一个词语在本篇文章出现的频率(TF)高,并且在其他文章出现少(即反文档频率IDF高),
  * 那么就可以认为这个词语是本篇文章的关键词,因为它具有很好的区分和代表能力
  **/
object MLLibDemo extends App with LogSupport {
  val warehouseLocation = System.getProperty("user.dir") + "/" + "spark-warehouse"
  val spark = SparkSession
    .builder()
    .master("local[*]")
    .appName("RddConvertToDataFrame")
    .config("spark.sql.warehouse.dir", warehouseLocation)
    .enableHiveSupport()
    .getOrCreate()
  val sc = spark.sparkContext

  import spark.implicits._

  //TODO DEMO 1
  val sentenceData = spark.createDataFrame(Seq(
    (0, "Hi I heard about Spark"),
    (0, "I wish Java could use case classes"),
    (1, "Logistic regression models are neat")
  )).toDF("label", "sentence")

  val tokenizer: Tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words")
  val wordsData: DataFrame = tokenizer.transform(sentenceData)
  val hashingTF: HashingTF = new HashingTF().setInputCol("words").setOutputCol("rawFeatures").setNumFeatures(20)
  val featurizedDataV1: DataFrame = hashingTF.transform(wordsData)

  val idfV1: IDF = new IDF().setInputCol("rawFeatures").setOutputCol("features")
  val idfModelV1: IDFModel = idfV1.fit(featurizedDataV1)
  val rescaledDataV1: DataFrame = idfModelV1.transform(featurizedDataV1)
  rescaledDataV1.select("features", "label").take(3).foreach(x => log.info(s"$x"))

  //TODO DEMO 2 TF-IDF 词频特征提取
  val df = sc.parallelize(Seq(
    (0, Array("a", "b", "c", "a")),
    (1, Array("c", "b", "b", "c", "a")),
    (2, Array("a", "a", "c", "d")),
    (3, Array("c", "a", "b", "a", "a")),
    (4, Array("我", "爱", "旅行", "土耳其", "大理", "云南")),
    (5, Array("我", "爱", "学习")),
    (6, Array("胡歌", "优秀", "演员", "幽默", "责任感"))
  )).map(x => (x._1, x._2)).toDF("id", "words")
  df.show(false) //展示数据
  val hashModel = new HashingTF()
    .setInputCol("words")
    .setOutputCol("rawFeatures")
    .setNumFeatures(Math.pow(2, 20).toInt)
  val featurizedData = hashModel.transform(df)
  featurizedData.show(false) //展示数据
  val df3: DataFrame = sc.parallelize(Seq(
    (0, Array("a", "a", "c", "d")),
    (1, Array("c", "a", "b", "a", "a"))
  )).map(x => (x._1, x._2)).toDF("id", "words")
  hashModel.transform(df3).show(false)
  val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
  val idfModel = idf.fit(featurizedData)
  val rescaledData: DataFrame = idfModel.transform(featurizedData)
  rescaledData.select("words", "features").show(false)
  try {
    val fileOut: FileOutputStream = new FileOutputStream("idf.jserialized")
    val out: ObjectOutputStream = new ObjectOutputStream(fileOut)
    out.writeObject(idfModel)
    out.close()
    fileOut.close()
    System.out.println("\nSerialization Successful... Checkout your specified output file..\n")
  } catch {
    case foe: FileNotFoundException => foe.printStackTrace()
    case ioe: IOException => ioe.printStackTrace()
  }

  val fos = new FileOutputStream("model.obj")
  val oos = new ObjectOutputStream(fos)
  oos.writeObject(idfModel)
  oos.close()
  val fis = new FileInputStream("model.obj")
  val ois = new ObjectInputStream(fis)
  val newModel = ois.readObject().asInstanceOf[IDFModel]
  val df2 = sc.parallelize(Seq(
    (0, Array("a", "b", "c", "a")),
    (1, Array("c", "b", "b", "c", "a")),
    (2, Array("我", "爱", "旅行", "土耳其", "大理", "云南")),
    (3, Array("我", "爱", "工作")),
    (4, Array("胡歌", "优秀", "演员", "幽默", "责任感"))
  )).map(x => (x._1, x._2)).toDF("id", "words")
  val hashModel2 = new HashingTF()
    .setInputCol("words")
    .setOutputCol("rawFeatures")
    .setNumFeatures(Math.pow(2, 20).toInt)
  val featurizedData2 = hashModel2.transform(df2)
  val rescaledData2 = newModel.transform(featurizedData2)
  rescaledData2.select("words", "features").show(false)


  spark.stop()
}

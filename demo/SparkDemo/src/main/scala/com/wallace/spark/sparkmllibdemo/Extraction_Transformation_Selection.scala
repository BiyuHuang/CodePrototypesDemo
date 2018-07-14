package com.wallace.spark.sparkmllibdemo

import com.wallace.common.CreateSparkSession
import org.apache.spark.ml.feature._
import org.apache.spark.sql.SparkSession

/**
  * Created by Wallace on 2016/11/10.
  */
object Extraction_Transformation_Selection extends CreateSparkSession {
  def main(args: Array[String]): Unit = {
    val spark = createSparkSession("Extraction_Transformation_Selection_Demo")
    //    hashingTF(spark)
    //    word2Vec(spark)
    //    countVectorizer(spark)
    //    tokenizer(spark)
    //    binarizer(spark)
    pca(spark)
  }

  def pca(spark: SparkSession): Unit = {
    import org.apache.spark.ml.linalg.Vectors
    val data = Array(Vectors.sparse(5, Seq((1, 1.0), (3, 7.0))),
      Vectors.dense(2.0, 0.0, 3.0, 4.0, 5.0),
      Vectors.dense(4.0, 0.0, 0.0, 6.0, 7.0))
    val df = spark.createDataFrame(data.map(Tuple1.apply)).toDF("features")
    df.show()
    val pca = new PCA()
      .setInputCol("features")
      .setOutputCol("pcaFeatures")
      .setK(4)
      .fit(df)

    val result = pca.transform(df).select("pcaFeatures")
    result.show(false)
  }

  def hashingTF(spark: SparkSession): Unit = {
    val sentenceData = spark.createDataFrame(Seq(
      (0, "Hi I heard about Spark"),
      (0, "I wish Java could use case classes"),
      (1, "Logistic regression models are neat")
    )).toDF("label", "sentence")

    val tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words")
    val wordsData = tokenizer.transform(sentenceData)
    val hashingTF = new HashingTF()
      .setInputCol("words").setOutputCol("rawFeatures").setNumFeatures(20)
    val featurizedData = hashingTF.transform(wordsData)
    // alternatively, CountVectorizer can also be used to get term frequency vectors

    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
    val idfModel = idf.fit(featurizedData)
    val rescaledData = idfModel.transform(featurizedData)
    rescaledData.select("features", "label").take(3).foreach(x => log.info(x.toString))
  }

  def word2Vec(spark: SparkSession): Unit = {
    // Input data: Each row is a bag of words from a sentence or document.
    val documentDF = spark.createDataFrame(Seq(
      "Hi I heard about Spark".split(" "),
      "I wish Java could use case classes".split(" "),
      "Logistic regression models are neat".split(" ")
    ).map(Tuple1.apply)).toDF("text")

    // Learn a mapping from words to Vectors.
    val word2Vec = new Word2Vec()
      .setInputCol("text")
      .setOutputCol("result")
      .setVectorSize(3)
      .setMinCount(0)
    val model = word2Vec.fit(documentDF)
    val result = model.transform(documentDF)
    result.select("result").take(3).foreach(x => log.info(x.toString))
  }

  def countVectorizer(spark: SparkSession): Unit = {
    val df = spark.createDataFrame(Seq(
      (0, Array("a", "b", "c")),
      (1, Array("a", "b", "b", "c", "a"))
    )).toDF("id", "words")

    // fit a CountVectorizerModel from the corpus
    val cvModel: CountVectorizerModel = new CountVectorizer()
      .setInputCol("words")
      .setOutputCol("features")
      .setVocabSize(3)
      .setMinDF(2)
      .fit(df)

    // alternatively, define CountVectorizerModel with a-priori vocabulary
    val cvm = new CountVectorizerModel(Array("a", "b", "c"))
      .setInputCol("words")
      .setOutputCol("features")

    cvModel.transform(df).select("features").show()
  }

  def tokenizer(spark: SparkSession): Unit = {
    val sentenceDataFrame = spark.createDataFrame(Seq(
      (0, "Hi I heard about Spark"),
      (1, "I wish Java could use case classes"),
      (2, "Logistic,regression,models,are,neat")
    )).toDF("label", "sentence")

    val tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words")
    val regexTokenizer = new RegexTokenizer()
      .setInputCol("sentence")
      .setOutputCol("words")
      .setPattern("\\W") // alternatively .setPattern("\\w+").setGaps(false)

    val tokenized = tokenizer.transform(sentenceDataFrame)
    tokenized.select("words", "label").take(3).foreach(x => log.info(x.toString))
    val regexTokenized = regexTokenizer.transform(sentenceDataFrame)
    regexTokenized.select("words", "label").take(3).foreach(x => log.error(x.toString()))
  }

  def stopWordRemover(spark: SparkSession): Unit = {
    import org.apache.spark.ml.feature.StopWordsRemover
    val remover = new StopWordsRemover()
      .setInputCol("raw")
      .setOutputCol("filtered")

    val dataSet = spark.createDataFrame(Seq(
      (0, Seq("I", "saw", "the", "red", "baloon")),
      (1, Seq("Mary", "had", "a", "little", "lamb"))
    )).toDF("id", "raw")

    remover.transform(dataSet).show()
  }

  def nGram(spark: SparkSession): Unit = {
    import org.apache.spark.ml.feature.NGram

    val wordDataFrame = spark.createDataFrame(Seq(
      (0, Array("Hi", "I", "heard", "about", "Spark")),
      (1, Array("I", "wish", "Java", "could", "use", "case", "classes")),
      (2, Array("Logistic", "regression", "models", "are", "neat"))
    )).toDF("label", "words")

    val ngram = new NGram().setInputCol("words").setOutputCol("ngrams")
    val ngramDataFrame = ngram.transform(wordDataFrame)
    ngramDataFrame.take(3).map(_.getAs[Stream[String]]("ngrams").toList).foreach(x => log.info(x.toString()))
  }

  /** Binarizer */
  def binarizer(spark: SparkSession): Unit = {
    val data = Array((0, 0.1), (1, 0.8), (2, 0.2))
    val dataFrame = spark.createDataFrame(data).toDF("id", "feature")
    val binarizer = new Binarizer()
      .setInputCol("feature")
      .setOutputCol("binarized_feature")
      .setThreshold(0.5)
    val binarizedDataFrame = binarizer.transform(dataFrame)

    log.info(s"Binarizer output with Threshold = ${binarizer.getThreshold}")
    binarizedDataFrame.show()
  }

  //  PolynomialExpansion
}

package com.wallace.spark.sparkdemo.structuredstreamingdemo

import com.wallace.common.{CreateSparkSession, Using}
import org.apache.spark.sql.streaming.StreamingQuery
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

/**
  * Created by wallace on 2020/3/9.
  */
object StructuredStreamingDemo extends CreateSparkSession with Using {
  private val _spark: SparkSession = createSparkSession("StructuredStreamingDemo")

  def main(args: Array[String]): Unit = {
    import _spark.implicits._
    val lines: DataFrame = _spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", "9999")
      .load()

    val words: Dataset[String] = lines.as[String].flatMap(_.split("""\s+"""))

    val wordsCount: DataFrame = words.groupBy("value").count()

    val query: StreamingQuery = wordsCount.writeStream.outputMode("complete").format("console").start()

    query.awaitTermination()
  }
}

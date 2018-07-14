package com.wallace.spark.sparkstreaming.kafkademo

import com.wallace.common.LogSupport
import com.wallace.spark.sparkstreaming.MessageConsumer.createStream
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.{CanCommitOffsets, ConsumerStrategy, HasOffsetRanges, OffsetRange}
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}

import scala.util.control.NonFatal

/**
  * Created by 10192057 on 2017/8/7.
  */
object KafkaSSConsumerDemo extends LogSupport {
  private val DEFAULT_DURATION = 5L

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf()
    sparkConf.setMaster("local[*]")
      .setAppName("Kafka-SparkStreaming-Consumer-Demo")
      .set("spark.sql.shuffle.partitions", "5")
      .set("spark.streaming.kafka.maxRatePerPartition", "1000")
    val ssc = new StreamingContext(sparkConf, Seconds(DEFAULT_DURATION))
    ssc.checkpoint("./")

    val topics: Set[String] = Set("test_hby")
    val kafkaParams: Map[String, Object] = Map[String, Object](
      "bootstrap.servers" -> "10.9.234.31:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "wallace_temp",
      "auto.offset.reset" -> "earliest", //earliest消费历史数据, latest消费最新数据
      "enable.auto.commit" -> (true: java.lang.Boolean)
    )
    val subScribe: ConsumerStrategy[String, String] = Subscribe[String, String](topics, kafkaParams)
    val stream: InputDStream[ConsumerRecord[String, String]] = createStream(ssc, PreferConsistent, subScribe)
    stream.foreachRDD {
      (rdd, time) =>
        val batchTime: Time = time
        val offsetRanges: Array[OffsetRange] = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
        log.warn(s"ID: ${batchTime.milliseconds / 1000}")
        offsetRanges.foreach {
          offset =>
            log.warn(
              s"""
                 |Topic: ${offset.topic}
                 |Partition: ${offset.partition}
                 |FromOffset: ${offset.fromOffset}
                 |UntilOffset: ${offset.untilOffset}""".stripMargin)
        }
        //        rdd.foreachPartition {
        //          _ =>
        //            val offset: OffsetRange = offsetRanges(TaskContext.get.partitionId())
        //            log.warn(
        //              s"""
        //                 |Topic: ${offset.topic}
        //                 |Partition: ${offset.partition}
        //                 |FromOffset: ${offset.fromOffset}
        //                 |UntilOffset: ${offset.untilOffset}""".stripMargin)
        //        }
        stream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
    }
    stream.map(x => x.value().hashCode).foreachRDD(rdd => log.warn(s"[KafkaSparkStreamingConsumerDemo] Max HashCode: ${rdd.max()}."))

    ssc.start()

    try {
      ssc.awaitTermination()
    } catch {
      case NonFatal(e) =>
        log.error(s"[KafkaSparkStreamingConsumerDemo] Catch NonFatal Exception: ${e.getMessage}.")
        ssc.stop(stopSparkContext = true, stopGracefully = true)
    }
  }
}

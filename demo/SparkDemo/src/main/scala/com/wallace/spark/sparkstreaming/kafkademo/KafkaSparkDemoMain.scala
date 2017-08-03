package com.wallace.spark.sparkstreaming.kafkademo

import com.wallace.common.LogSupport
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.{SparkConf, TaskContext}

/**
  * Created by Wallace on 2016/4/20.
  */
object KafkaSparkDemoMain extends LogSupport {

  private val DEFAULT_DURATION: Long = 5000L
  private val topics = Set("test_hby") // 消费的kafka数据的topic
  private val updateFunc: (Seq[String], Option[String]) => Some[String] = (currentValues: Seq[String], preValue: Option[String]) => {
    val curr = currentValues
    val pre: Object = preValue.getOrElse(curr)
    Some(Option(pre).mkString("\r").split("\r").head)
  }

  private val kafkaParams: Map[String, Object] = Map[String, Object](
    "bootstrap.servers" -> "10.9.234.32:9092,10.9.234.35:9092",
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    "group.id" -> "wallace_temp",
    "auto.offset.reset" -> "latest", // earliest消费历史数据, latest消费最新数据
    "enable.auto.commit" -> (true: java.lang.Boolean)
  )
  private val subScribe: ConsumerStrategy[String, String] = Subscribe[String, String](topics, kafkaParams)


  def main(args: Array[String]): Unit = {
    val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("kafka-spark-demo")
    val scc = new StreamingContext(sparkConf, Duration(DEFAULT_DURATION))
    scc.checkpoint(".") // 因为使用到了updateStateByKey,所以必须要设置checkpoint
    val stream: InputDStream[ConsumerRecord[String, String]] = createStream(scc, PreferConsistent, subScribe)
    //    stream.map(record => (record.key(), record.value()))
    //      .map(_._2) // 取出value
    //      .flatMap(_.split(" ")) // 将字符串使用空格分隔
    //      .map(r => (r(0), r)) // 每个单词映射成一个pair
    //      .updateStateByKey[String](updateFunc) // 用当前batch的数据区更新已有的数据
    //      .print(10) // 打印前10个数据

    stream.foreachRDD {
      rdd =>
        val offsetRange: Array[OffsetRange] = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
        rdd.foreachPartition {
          _ =>
            val offset = offsetRange(TaskContext.get.partitionId)
            log.info(
              s"""
                 |PartitionID: ${TaskContext.get.partitionId}
                 |MetaData: ${offset.toString()}
                 |Topic: ${offset.topic}
                 |Partition: ${offset.partition}
                 |FormOffset: ${offset.fromOffset}
                 |UntilOffset: ${offset.untilOffset}
                 |Count: ${offset.count()}
               """.stripMargin)
        }
    }
    //    stream.map(record => (record.key(), record.value(), record.partition(), record.offset())).foreachRDD {
    //      rdd =>
    //        rdd.foreach {
    //          x =>
    //            log.error(s"####: Partition: ${x._3}, Offset: ${x._4}.")
    //        }
    //      // rdd.persist()
    //    }
    scc.start() // 真正启动程序
    scc.awaitTermination() //阻塞等待
  }

  /**
    * 创建一个从kafka获取数据的流.
    *
    * @param scc              spark streaming上下文
    * @param locationStrategy consumer调度分区的位置策略
    * @param subScribe        consumer的消费策略
    * @return
    */
  def createStream(
                    scc: StreamingContext,
                    locationStrategy: LocationStrategy,
                    subScribe: ConsumerStrategy[String, String]): InputDStream[ConsumerRecord[String, String]] = {
    KafkaUtils.createDirectStream[String, String](scc, locationStrategy, subScribe)
  }
}

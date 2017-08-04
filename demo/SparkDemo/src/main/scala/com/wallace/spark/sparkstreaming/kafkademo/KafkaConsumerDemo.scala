package com.wallace.spark.sparkstreaming.kafkademo

import java.util
import java.util.Properties

import com.wallace.common.LogSupport
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.{PartitionInfo, TopicPartition}

import scala.collection.JavaConversions._

/**
  * Created by 10192057 on 2017/8/4.
  * KafkaConsumer Demo
  * Kafka 0.10.0.1
  */
object KafkaConsumerDemo extends LogSupport {
  //  val tpParam: TopicCommandOptions = new TopicCommandOptions(Array(
  //    "--partitions", "1",
  //    "--replication-factor", "2",
  //    "--disable-rack-aware",
  //    "--topic", "foo"))
  //
  //  val zkConnect: (ZkClient, ZkConnection) = ZkUtils.createZkClientAndConnection("localhost:2181", 6 * 1000, 10 * 1000)
  //  val zkUtil = new ZkUtils(zkConnect._1, zkConnect._2, true)
  // TopicCommand.createTopic(zkUtil, tpParam)
  private val topics = Set("test_hby") // 消费的kafka数据的topic
  private val p0 = new TopicPartition(topics.head, 0)
  private val p1 = new TopicPartition(topics.head, 1)

  def main(args: Array[String]): Unit = {
    val consumer: KafkaConsumer[String, String] = createConsumer
    consumer.assign(List(p0, p1))
    val partitions: util.List[PartitionInfo] = consumer.partitionsFor(topics.head)
    partitions.map(x => x.toString).foreach(p => log.error("[KafkaConsumerDemo] %s".format(p)))
    val record: ConsumerRecords[String, String] = consumer.poll(2048L)
    record.map(x => (x.key(), x.value())).foreach(r => log.error(s"[KafkaConsumerDemo]\nKey: %s\nValue: %s".format(r._1, r._2)))
  }

  private def createConsumer: KafkaConsumer[String, String] = {
    val props = new Properties
    props.put("bootstrap.servers", "10.9.234.32:9092,10.9.234.35:9092")
    props.put("group.id", "wallace_temp")
    props.put("enable.auto.commit", "false") //关闭自动commit
    props.put("session.timeout.ms", "30000")
    props.put("auto.offset.reset", "earliest")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    new KafkaConsumer(props)
  }
}

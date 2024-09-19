package com.wallace.spark.sparkstreaming.kafkademo

import com.wallace.common.Using
import kafka.common.OffsetAndMetadata
import kafka.coordinator.{BaseKey, GroupMetadataManager}
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.{PartitionInfo, TopicPartition}

import java.nio.ByteBuffer
import java.util
import java.util.Properties
import scala.collection.convert.ImplicitConversions.`iterable AsScalaIterable`
import scala.collection.mutable
import scala.jdk.CollectionConverters.{asScalaBufferConverter, iterableAsScalaIterableConverter, seqAsJavaListConverter, setAsJavaSetConverter}

/**
  * Created by 10192057 on 2017/8/4.
  * KafkaConsumer Demo
  * Kafka 0.10.0.1
  */
object KafkaConsumerDemo extends Using {
  //  val tpParam: TopicCommandOptions = new TopicCommandOptions(Array(
  //    "--partitions", "1",
  //    "--replication-factor", "2",
  //    "--disable-rack-aware",
  //    "--topic", "foo"))
  //
  //  val zkConnect: (ZkClient, ZkConnection) = ZkUtils.createZkClientAndConnection("10.9.234.34:2181,10.9.234.35:2181,10.9.234.32:2181/kafka", 6 * 1000, 10 * 1000)
  //  val zkUtil = new ZkUtils(zkConnect._1, zkConnect._2, true)

  private val topics = Set("__consumer_offsets", "test_hby") // 消费的kafka数据的topic
  private val p0 = new TopicPartition(topics.head, 0)
  private val p1 = new TopicPartition(topics.head, 1)

  def main(args: Array[String]): Unit = {
    //    TopicCommand.createTopic(zkUtil, tpParam)
    using(createConsumer[ByteBuffer, ByteBuffer]("org.apache.kafka.common.serialization.ByteBufferDeserializer", "org.apache.kafka.common.serialization.ByteBufferDeserializer")) {
      consumer =>
        val p: TopicPartition = new TopicPartition(topics.head, "wallace_temp".hashCode % 30)
        consumer.assign(Set(p).asJava)
        //consumer.seekToBeginning(Set(p))
        // consumer.seekToEnd(parts)
        //        parts.foreach {
        //          p =>
        //            consumer.seek(p, 0L)
        //        }
        val record: ConsumerRecords[ByteBuffer, ByteBuffer] = consumer.poll(20480L)
        record.map(x => (x.key(), x.value())).foreach {
          r =>
            val key: BaseKey = GroupMetadataManager.readMessageKey(r._1)
            val value: OffsetAndMetadata = GroupMetadataManager.readOffsetMessageValue(r._2)
            log.info(s"$key, $value")
        }
    }

    using(createConsumer[String, String]("org.apache.kafka.common.serialization.StringDeserializer", "org.apache.kafka.common.serialization.StringDeserializer")) {
      consumer =>
        //consumer.assign(List(p0, p1))
        val partitions: util.List[PartitionInfo] = consumer.partitionsFor(topics.last)
        val parts: mutable.Seq[TopicPartition] = partitions.asScala.map {
          p =>
            new TopicPartition(p.topic(), p.partition())
        }
        consumer.assign(parts.asJava)
        //consumer.seekToBeginning(parts)
        // consumer.seekToEnd(parts)
        //        parts.foreach {
        //          p =>
        //            consumer.seek(p, 0L)
        //        }

        partitions.map(x => x.toString).foreach(p => log.error("[KafkaConsumerDemo] %s".format(p)))
        val record: ConsumerRecords[String, String] = consumer.poll(20480L)
        record.asScala.map(x => (x.key(), x.value())).foreach {
          r =>
            log.error(s"[KafkaConsumerDemo]\nKey: %s\nValue: %s".format(r._1, r._2))
        }
        consumer.commitAsync()
    }
  }

  private def createConsumer[K, V](kDeserializer: String, vDeserializer: String): KafkaConsumer[K, V] = {
    val props = new Properties
    props.put("bootstrap.servers", "10.9.234.31:9092")
    props.put("group.id", "wallace_temp")
    props.put("enable.auto.commit", "true") //关闭自动commit
    props.put("session.timeout.ms", "30000")
    props.put("auto.offset.reset", "earliest")
    props.put("key.deserializer", kDeserializer)
    props.put("value.deserializer", vDeserializer)
    new KafkaConsumer(props)
  }
}

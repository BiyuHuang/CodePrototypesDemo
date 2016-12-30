package org.apache.spark.streaming.kafka

import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.Decoder
import org.apache.spark.SparkException
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.KafkaCluster.{Err, LeaderOffset}

import scala.reflect.ClassTag

/**
  * Created by Wallace on 2016/11/23.
  */
class KafkaManager(val kafkaParams: Map[String, String]) extends Serializable {
  private def kc = new KafkaCluster(kafkaParams)

  private def setOrUpdateOffsets(topics: Set[String], groupId: String) = {
    topics.foreach {
      topic =>
        var hasConsumed = true
        val partitionsE = kc.getPartitions(Set(topic))
        if (partitionsE.isLeft) throw new SparkException("Get kafka partition failed: ")
        val partitions = partitionsE.right.get
        val consumerOffsetsE: Either[Err, Map[TopicAndPartition, Long]] = kc.getConsumerOffsets(groupId, partitions)
        if (consumerOffsetsE.isLeft) hasConsumed = false
        if (hasConsumed) {
          val earliestLeaderOffsets: Map[TopicAndPartition, LeaderOffset] = kc.getEarliestLeaderOffsets(partitions).right.get
          val consumerOffsets: Map[TopicAndPartition, Long] = consumerOffsetsE.right.get
          var offsets: Map[TopicAndPartition, Long] = Map()
          consumerOffsets.foreach({
            case (tp, n) =>
              val earliestLeaderOffset = earliestLeaderOffsets(tp).offset
              if (n < earliestLeaderOffset) {
                println("Consumer group: " + groupId + ",Topic: " + tp.topic + ",Partition: " + tp.partition + " offsets已经过时，更新为 " + earliestLeaderOffset)
                offsets += (tp -> earliestLeaderOffset)
              }
          })
          if (offsets.nonEmpty) {
            kc.setConsumerOffsets(groupId, offsets)
          }
        } else {
          val reset = kafkaParams.get("auto.offfset.reset").map(_.toLowerCase)
          var leaderOffsets: Map[TopicAndPartition, LeaderOffset] = null
          if (reset.contains("smallest")) {
            leaderOffsets = kc.getEarliestLeaderOffsets(partitions).right.get
          } else {
            leaderOffsets = kc.getLatestLeaderOffsets(partitions).right.get
          }
          val offsets = leaderOffsets.map {
            case (tp, offset) => (tp, offset.offset)
          }
          kc.setConsumerOffsets(groupId, offsets)
        }
    }
  }

  /**
    **/
  def createDirectStream[K: ClassTag, V: ClassTag, KD <: Decoder[K] : ClassTag, VD <: Decoder[V] : ClassTag](ssc: StreamingContext, topics: Set[String]) = {
    val groupId = kafkaParams("group.id")
    setOrUpdateOffsets(topics, groupId)
    val partitionsE = kc.getPartitions(topics)
    if (partitionsE.isLeft) throw new SparkException("Get kafka partition failed: ")
    val partitions = partitionsE.right.get
    val consumerOffsetsE = kc.getConsumerOffsets(groupId, partitions)
    if (consumerOffsetsE.isLeft) throw new SparkException("Get kafka consumer offsets failed: ")
    val consumerOffsets = consumerOffsetsE.right.get
    UdfKafkaUtils.createDirectStream[K, V, KD, VD, (K, V)](ssc, kafkaParams, consumerOffsets, (mmd: MessageAndMetadata[K, V]) => (mmd.key(), mmd.message()))
  }

  /**
    * 更新zookeeper上的消费offsets
    *
    * @param rdd
    */
  def updateZKOffsets(rdd: RDD[(String, String)]): Unit = {
    val groupId = kafkaParams("group.id")
    val offsetsList = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

    for (offsets <- offsetsList) {
      val topicAndPartition = TopicAndPartition(offsets.topic, offsets.partition)
      val o = kc.setConsumerOffsets(groupId, Map((topicAndPartition, offsets.untilOffset)))
      if (o.isLeft) {
        println(s"Error updating the offset to Kafka cluster: ${o.left.get}")
      }
    }
  }
}

object UdfKafkaUtils {
  def createDirectStream[
  K: ClassTag,
  V: ClassTag,
  KD <: Decoder[K] : ClassTag,
  VD <: Decoder[V] : ClassTag,
  R: ClassTag](
                ssc: StreamingContext,
                kafkaParams: Map[String, String],
                fromOffsets: Map[TopicAndPartition, Long],
                messageHandler: MessageAndMetadata[K, V] => R
              ): InputDStream[R] = {
    val cleanedHandler = ssc.sc.clean(messageHandler)
    new MyDirectKafkaInputDStream[K, V, KD, VD, R](
      ssc, kafkaParams, fromOffsets, cleanedHandler)
  }
}


private[streaming]
class MyDirectKafkaInputDStream[K: ClassTag,
V: ClassTag,
U <: Decoder[K] : ClassTag,
T <: Decoder[V] : ClassTag,
R: ClassTag](ssc_ : StreamingContext,
             override val kafkaParams: Map[String, String],
             override val fromOffsets: Map[TopicAndPartition, Long],
             messageHandler: MessageAndMetadata[K, V] => R)
  extends DirectKafkaInputDStream[K, V, U, T, R](ssc_, kafkaParams, fromOffsets, messageHandler) {
  override protected def clamp(leaderOffsets: Map[TopicAndPartition, LeaderOffset]): Map[TopicAndPartition, LeaderOffset] = {
    maxMessagesPerPartition.map {
      mmp =>
        leaderOffsets.map {
          case (tp, lo) =>
            tp -> lo.copy(offset = Math.min(currentOffsets(tp) + mmp, lo.offset))
        }
    }.getOrElse(leaderOffsets)
  }

  val maxRateLimitPerPartition = 100

  override protected val maxMessagesPerPartition: Option[Long] = {
    //    val estimatedRateLimit = rateController.map(_.getLatestRate().toInt)
    val estimatedRateLimit = Some(10000)
    val numPartitions = currentOffsets.keys.size

    val effectiveRateLimitPerPartition = estimatedRateLimit
      .filter(_ > 0)
      .map {
        limit =>
          if (maxRateLimitPerPartition > 0) {
            Math.min(maxRateLimitPerPartition, limit / numPartitions)
          } else {
            limit / numPartitions
          }
      }.getOrElse(maxRateLimitPerPartition)

    if (effectiveRateLimitPerPartition > 0) {
      val secsPerBatch = context.graph.batchDuration.milliseconds.toDouble / 1000
      Some((secsPerBatch * effectiveRateLimitPerPartition).toLong)
    } else {
      None
    }
  }
}

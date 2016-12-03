//package org.apache.spark.streaming.kafka
//
//import kafka.common.TopicAndPartition
//import kafka.message.MessageAndMetadata
//import kafka.serializer.Decoder
//import org.apache.spark.streaming.StreamingContext
//import org.apache.spark.streaming.dstream.InputDStream
//import org.apache.spark.streaming.kafka.KafkaCluster.LeaderOffset
//
//import scala.reflect.ClassTag
//
///**
//  * Created by Wallace on 2016/11/23.
//  */
//object UdfKafkaUtils {
//  def createDirectStream[
//  K: ClassTag,
//  V: ClassTag,
//  KD <: Decoder[K] : ClassTag,
//  VD <: Decoder[V] : ClassTag,
//  R: ClassTag](
//                ssc: StreamingContext,
//                kafkaParams: Map[String, String],
//                fromOffsets: Map[TopicAndPartition, Long],
//                messageHandler: MessageAndMetadata[K, V] => R
//              ): InputDStream[R] = {
//    val cleanedHandler = ssc.sc.clean(messageHandler)
//    new MyDirectKafkaInputDStream[K, V, KD, VD, R](
//      ssc, kafkaParams, fromOffsets, cleanedHandler)
//  }
//}
//
//
////private[streaming]
////class MyDirectKafkaInputDStream[K: ClassTag,
////V: ClassTag,
////U <: Decoder[K] : ClassTag,
////T <: Decoder[V] : ClassTag,
////R: ClassTag](ssc_ : StreamingContext,
////             override val kafkaParams: Map[String, String],
////             override val fromOffsets: Map[TopicAndPartition, Long],
////             messageHandler: MessageAndMetadata[K, V] => R)
////  extends DirectKafkaInputDStream[K, V, U, T, R](ssc_, kafkaParams, fromOffsets, messageHandler)
////{
////  override protected def clamp(leaderOffsets: Map[TopicAndPartition, LeaderOffset]): Map[TopicAndPartition, LeaderOffset] = {
////    maxMessagesPerPartition.map {
////      mmp =>
////        leaderOffsets.map {
////          case (tp, lo) =>
////            tp -> lo.copy(offset = Math.min(currentOffsets(tp) + mmp, lo.offset))
////        }
////    }.getOrElse(leaderOffsets)
////  }
////
////  val maxRateLimitPerPartition = 100
////
////  override protected val maxMessagesPerPartition: Option[Long] = {
////    //    val estimatedRateLimit = rateController.map(_.getLatestRate().toInt)
////    val estimatedRateLimit = Some(10000)
////    val numPartitions = currentOffsets.keys.size
////
////    val effectiveRateLimitPerPartition = estimatedRateLimit
////      .filter(_ > 0)
////      .map {
////        limit =>
////          if (maxRateLimitPerPartition > 0) {
////            Math.min(maxRateLimitPerPartition, limit / numPartitions)
////          } else {
////            limit / numPartitions
////          }
////      }.getOrElse(maxRateLimitPerPartition)
////
////    if (effectiveRateLimitPerPartition > 0) {
////      val secsPerBatch = context.graph.batchDuration.milliseconds.toDouble / 1000
////      Some((secsPerBatch * effectiveRateLimitPerPartition).toLong)
////    } else {
////      None
////    }
////  }
////}

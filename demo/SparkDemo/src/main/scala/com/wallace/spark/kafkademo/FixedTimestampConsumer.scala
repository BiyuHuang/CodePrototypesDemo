//package org.apache.spark.streaming.kafka.udf
//
//import com.wallace.spark.CreateSparkSession
//import kafka.common.TopicAndPartition
//import kafka.message.MessageAndMetadata
//import kafka.serializer.StringDecoder
//import org.apache.spark.streaming.kafka.KafkaCluster.Err
//import org.apache.spark.streaming.kafka.{KafkaCluster, KafkaUtils}
//import org.apache.spark.streaming.{Duration, StreamingContext}
//
///**
//  * Created by Wallace on 2017/1/1.
//  */
//object FixedTimestampConsumer extends CreateSparkSession {
//  val spark = createSparkSession()
//  val duration: Long = 5000
//  val ssc = new StreamingContext(spark.sparkContext, Duration(duration))
//  ssc.checkpoint(".")
//
//  val topics = Set("kafka-spark-demo")
//  val kafkaParam = Map(
//    "metadata.broker.list" -> "localhost:9092",
//    "group.id" -> "demo"
//  )
//  //val streamData = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParam, topics)
//  val kc = new KafkaCluster(kafkaParam)
//  val topicPartsInfo: Either[Err, Set[TopicAndPartition]] = kc.getPartitions(topics)
//
//  /** 指定时间戳消费 */
//  if (topicPartsInfo.isRight) {
//    val leaderOffsets = kc.getLeaderOffsets(topicPartsInfo.right.get, duration)
//    if (leaderOffsets.isRight) {
//      val offsets = leaderOffsets.right.get.map { case (k, v) => (k, v.offset) } //获取到指定时间戳的offsets信息, 将 offsets信息 作为参数传入 createDirectStream 即可
//      val messageHandler: (MessageAndMetadata[String, String]) => (String, String) = (mmd: MessageAndMetadata[String, String]) => (mmd.topic, mmd.message())
//      val streamData = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder, (String, String)](ssc, kafkaParam, offsets, messageHandler)
//      streamData.transform(rdd => rdd.coalesce(25)).foreachRDD {
//        rdd =>
//          rdd.map(_._2.split(" ", -1))
//          rdd.saveAsTextFile("/demo/fixedTimestampConsumer/")
//      }
//    } else {
//      /** 在本函数内部处理错误，如果有错误抛出异常 */
//      throw new RuntimeException(s"[FixedTimestampConsumer] Exception when MTKafkaUtils #getLeaderOffsets# ${leaderOffsets.left.get}.")
//    }
//  } else {
//    log.error(s"[FixedTimestampConsumer] Failed to get Partitions information for $topics. ### ${topicPartsInfo.left.get} ###")
//  }
//}

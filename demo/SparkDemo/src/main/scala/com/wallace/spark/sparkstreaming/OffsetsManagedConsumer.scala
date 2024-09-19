//package com.wallace.spark.sparkstreaming
//
//import java.net.ConnectException
//
//import com.wallace.common.CreateSparkSession
//import kafka.serializer.StringDecoder
//import org.apache.spark.sql.SparkSession
//import org.apache.spark.streaming.kafka.KafkaManager
//import org.apache.spark.streaming.{Duration, StreamingContext}
//
///**
//  * Created by Wallace on 2016/12/31.
//  */
//object OffsetsManagedConsumer extends CreateSparkSession {
//  val spark: SparkSession = createSparkSession()
//  val scc = new StreamingContext(spark.sparkContext, Duration(5000))
//  scc.checkpoint(".")
//
//  val topics = Set("kafka-spark-demo")
//  val kafkaParam = Map(
//    "metadata.broker.list" -> "localhost:9092",
//    "group.id" -> "demo"
//  )
//
//  val kafkaManager = new KafkaManager(kafkaParam)
//  //val streamData = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](scc, kafkaParam, topics)
//  /** User Defined Function API */
//  val streamData = kafkaManager.createDirectStream[String, String, StringDecoder, StringDecoder](scc, topics)
//
//  streamData.transform(rdd => rdd.coalesce(25)).foreachRDD {
//    rdd =>
//      var actionResult: Boolean = true
//      try {
//        rdd.map {
//          row =>
//            row._2.split(" ", -1)
//        }
//      } catch {
//        case v if v.isInstanceOf[ConnectException] =>
//          log.warn("The Network loses connect.")
//        case e: Exception =>
//          actionResult = false
//          throw e
//      } finally {
//        actionResult match {
//          case true => kafkaManager.updateZKOffsets(rdd)
//          case false => log.error("Failed to update offsets to Zookeeper.")
//        }
//      }
//  }
//}
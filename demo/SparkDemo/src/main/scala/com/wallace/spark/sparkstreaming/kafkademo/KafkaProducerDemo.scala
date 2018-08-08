package com.wallace.spark.sparkstreaming.kafkademo

import java.nio.charset.Charset
import java.util
import java.util.{Timer, TimerTask}

import com.wallace.common.LogSupport
import com.wallace.common.timeformat.TimePara
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

import scala.io.Source

/**
  * Created by Wallace on 2016/5/5.
  */
object KafkaProducerDemo extends LogSupport {
  def main(args: Array[String]): Unit = {
    //    if (args.length < 3) {
    //      log.error("Usage: KafkaWordCountProducer <metadataBrokerList> <topic> <messagesPerSec>")
    //      System.exit(1)
    //    }
    val (brokers, topic, messagesPerSec) = ("207.246.109.109:9092", "test_hby", "1000")
    val timer = new Timer
    timer.schedule(new senderTimer(brokers, topic, messagesPerSec.toInt), 1000, 5000)
  }
}

class senderTimer(brokers: String, topic: String, messagesPerSec: Int) extends TimerTask with LogSupport {
  // Zookeeper connection properties
  val propsV1 = new util.HashMap[String, Object]()
  propsV1.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
  propsV1.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
    "org.apache.kafka.common.serialization.StringSerializer")
  propsV1.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
    "org.apache.kafka.common.serialization.StringSerializer")

  val producerV1 = new KafkaProducer[String, String](propsV1)

  val propsV2 = new util.HashMap[String, Object]()
  propsV2.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
  propsV2.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
    "org.apache.kafka.common.serialization.ByteArraySerializer")
  propsV2.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
    "org.apache.kafka.common.serialization.StringSerializer")
  val producerV2 = new KafkaProducer[String, Array[Byte]](propsV2)
  // Send some messages

  override def run(): Unit = {
    val file = Source.fromFile("demo/SparkDemo/data/DateProducer_2016-05-14_Test.csv", "UTF-8")
    val lines = file.getLines.toArray
    log.info(s"========== Start to send ${messagesPerSec * 5} message to Topic: [$topic] ==========")
    (1 to messagesPerSec * 5).foreach {
      _ =>
        val str: Array[String] = lines(scala.util.Random.nextInt(lines.length)).split(",", -1)
        try {
          val msg: String = s"""${TimePara.getCurrentDate},${str.drop(1).mkString(",")}"""
          val messageV1: ProducerRecord[String, String] = new ProducerRecord[String, String](topic, msg)
          val messageV2: ProducerRecord[String, Array[Byte]] = new ProducerRecord[String, Array[Byte]](topic + "_temp", msg.getBytes(Charset.forName("UTF8")))
          producerV1.send(messageV1)
          producerV2.send(messageV2)
        } catch {
          case e: Exception =>
            log.error(e.getMessage)
            throw e
        }
    }
    log.info(s"========== Succeed to Send message to Topic : [$topic] ==========")
  }
}

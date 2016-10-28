package com.wallace.spark.SparkSteaming

import java.text.SimpleDateFormat
import java.util
import java.util.{Date, Timer, TimerTask}

import com.wallace.spark.common.LogSupport
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

import scala.io.Source
import scala.util.Random

/**
  * Created by huangbiyu on 16-6-4.
  */
object MessageProducer {
  def main(args: Array[String]) {
    if (args.length < 1) {
      println(
        s"""
           |<Usage>: MessageProducer messagesPerSec[Int]
           |         MessageProducer 1000
           |""".stripMargin)
      System.exit(0)
    }
    val timer = new Timer
    val task = new MsgSender(args(0).toInt)
    timer.schedule(task, 1000, 5000)
  }
}

class MsgSender(numPerSec: Int) extends TimerTask with LogSupport {

  lazy val (brokers, topic, messagesPerSec, wordsPerMessage) = ("localhost:9092", "kafka-spark-demo-test", numPerSec, "10000")
  log.error(
    s"""
       |############ Kafka Params ################
       |[Brokers-List]: $brokers
       |[Topics]: $topic
       |[MessagePerSec]: $messagesPerSec
       |[WordsPerMessage]: $wordsPerMessage
       |##########################################
       """.stripMargin)

  override def run(): Unit = {
    // Zookeeper connection properties
    val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    val props = new util.HashMap[String, Object]()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
      "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
      "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    val srcFile = "data/Testing_Data_2016-10-03.csv"
    val data = Source.fromFile(srcFile, "UTF-8")
    val lines = data.getLines().toArray
    if (lines.nonEmpty) {
      val startTIme = System.currentTimeMillis()
      (1 to messagesPerSec).foreach {
        i => val line = lines(Random.nextInt(lines.length))
          .replaceFirst("([0-9]{4,4}-[0-9]{2,2}-[0-9]{2,2} [0-9]{2,2}:[0-9]{2,2}:[0-9]{2,2}.[0-9]{3,3})", s"${df.format(new Date(System.currentTimeMillis))}")
          //val line = s"${df.format(new Date(System.currentTimeMillis))}" + lines(Random.nextInt(lines.length)).split(",", -1).drop(1).mkString(",")
          //println("###################" + line)
          val message = new ProducerRecord[String, String](topic, null, line)
          producer.send(message)
      }
      val endTime = System.currentTimeMillis()
      log.error(s"[Sended $messagesPerSec Messages to Kafka Topics: $topic, Cost ${endTime - startTIme} ms.]")
      data.close()
    } else {
      log.warn(s"Source Files is Empty!Please Check!")
      data.close()
    }

  }
}
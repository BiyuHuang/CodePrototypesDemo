package com.wallace.spark.sparkstreaming

import java.text.SimpleDateFormat

import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by huangbiyu on 16-6-8.
  */

object MessageConsumer {
  val updateFunc = (currentValues: Seq[Int], preValue: Option[Int]) => {
    val curr = currentValues.sum
    val pre = preValue.getOrElse(0)
    Some(curr + pre)
  }
  val updateValueFunc = (curValue: Seq[String], preValue: Option[String]) => {
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    val srcTime: Long = curValue.map {
      x =>
        val temp = x.split(",", -1)
        sdf.parse(temp(MessageDetail.TIMESTAMP.id)).getTime
    }.max

    val targetTime = preValue.map {
      x =>
        val temp = x.split(",", -1)
        sdf.parse(temp(MessageDetail.TIMESTAMP.id)).getTime
    }.getOrElse(0L)

    val res: String = if (targetTime == 0 || srcTime >= targetTime) {
      curValue.maxBy {
        x =>
          val temp = x.split(",", -1)
          sdf.parse(temp(MessageDetail.TIMESTAMP.id)).getTime
      }
    } else {
      preValue.getOrElse("")
    }

    Some(res)
  }

  def main(args: Array[String]) {
    val sparkConf = new SparkConf()
    sparkConf.setMaster("local[*]")
      .setAppName("Kafka-spark-demo")
      .set("spark.sql.shuffle.partitions", "5")

    val scc = new StreamingContext(sparkConf, Seconds(5))
    scc.checkpoint("./")
    val topics = Set("kafka-spark-demo")
    val kafkaParam = Map(
      "metadata.broker.list" -> "localhost:9092", // kafka的broker list地址
      "group.id" -> "wallace_temp",
      "zookeeper.connect" -> "localhost:2181")

    // 2016-09-30 10:30:00.000,UEID,TEXT,REGION_ID,X_OFFSET,Y_OFFSET
    val stream: InputDStream[(String, String)] = createStream(scc, kafkaParam, topics)
    stream.map(_._2).map {
      x =>
        val tempContext: Array[String] = x.split(",", -1)
        val key = tempContext(MessageDetail.UE_ID.id)
        (key, x)
    }.updateStateByKey[String](updateValueFunc)

    //.updateStateByKey[String](updateValueFunc)


    //    val tempDStream = stream.map(_._2) // 取出value
    //      .flatMap(_.split("\n")) // 将字符串使用空格分隔
    //      .map(r => (r.mkString, 1)) // 每个单词映射成一个pair
    //      .updateStateByKey[Int](updateFunc) // 用当前batch的数据区更新已有的数据
    //    tempDStream.map(_._2).print() // 打印前10个数据
    //    tempDStream.window(Seconds(15), Seconds(5))

    scc.start() // 真正启动程序
    scc.awaitTermination() //阻塞等待
  }

  /**
    * 创建一个从kafka获取数据的流.
    *
    * @param scc        spark streaming上下文
    * @param kafkaParam kafka相关配置
    * @param topics     需要消费的topic集合
    * @return
    */
  def createStream(scc: StreamingContext, kafkaParam: Map[String, String], topics: Set[String]): InputDStream[(String, String)] = {
    KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](scc, kafkaParam, topics)
  }
}

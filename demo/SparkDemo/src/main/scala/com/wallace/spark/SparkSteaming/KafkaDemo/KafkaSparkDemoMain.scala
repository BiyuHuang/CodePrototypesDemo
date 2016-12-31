package com.wallace.spark.SparkSteaming.KafkaDemo

import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Duration, StreamingContext}

/**
  * Created by Wallace on 2016/4/20.
  */
object KafkaSparkDemoMain {
  def main(args: Array[String]) {
    val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("kafka-spark-demo")
    val scc = new StreamingContext(sparkConf, Duration(5000))
    scc.checkpoint(".") // 因为使用到了updateStateByKey,所以必须要设置checkpoint
    val topics = Set("kafka-spark-demo") //我们需要消费的kafka数据的topic
    val kafkaParam = Map(
      "metadata.broker.list" -> "localhost:9092" // kafka的broker list地址
    )

    val stream: InputDStream[(String, String)] = createStream(scc, kafkaParam, topics)
    stream.map(_._2) // 取出value
      .flatMap(_.split("\n")) // 将字符串使用空格分隔
      .map(r => (r(0), r)) // 每个单词映射成一个pair
      .updateStateByKey[String](updateFunc) // 用当前batch的数据区更新已有的数据
      .print(10) // 打印前10个数据

    stream.count().print()
    scc.start() // 真正启动程序
    scc.awaitTermination() //阻塞等待
  }

  val updateFunc: (Seq[String], Option[String]) => Some[String] = (currentValues: Seq[String], preValue: Option[String]) => {
    val curr = currentValues
    val pre: Object = preValue.getOrElse(curr)
    Some(Option(pre).mkString("\r").split("\r").head)
  }

  /**
    * 创建一个从kafka获取数据的流.
    *
    * @param scc        spark streaming上下文
    * @param kafkaParam kafka相关配置
    * @param topics     需要消费的topic集合
    * @return
    */
  def createStream(scc: StreamingContext, kafkaParam: Map[String, String], topics: Set[String]) = {
    KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](scc, kafkaParam, topics)
  }
}

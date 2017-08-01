package org.apache.spark.streaming.flume

import com.wallace.common.LogSupport
import org.apache.spark.SparkContext
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.util.control.NonFatal

/**
  * Created by Wallace on 2017/3/30.
  */
object SparkStreamingFlume extends LogSupport {
  private val DEFAULT_BATCH_DURATION: Int = 20

  private val DEFAULT_PART_NUM: Int = 30

  def main(args: Array[String]): Unit = {

    if (args.length < 2) {
      log.warn("please enter host and port")
      System.exit(1)
    }

    //val sc = new SparkContext("spark://centos.host1:7077", "Spark Streaming Flume Integration")
    val sc = new SparkContext("local[*]", "Spark Streaming Flume Integration")

    //创建StreamingContext，20秒一个批次
    val ssc = new StreamingContext(sc, Seconds(DEFAULT_BATCH_DURATION))

    val hostname = args(0)
    val port = args(1).toInt
    val storageLevel = StorageLevel.MEMORY_ONLY
    val flumeStream = FlumeUtils.createStream(ssc, hostname, port, storageLevel)
    val flumePollingStream = FlumeUtils.createPollingStream(ssc, hostname, port, storageLevel)
    flumeStream.count().map(cnt => "Received " + cnt + " flume events.").print()
    flumePollingStream.count().map(cnt => "Received " + cnt + " flume events.").print()

    flumeStream.foreachRDD {
      rdd =>
        rdd.coalesce(DEFAULT_PART_NUM).saveAsTextFile("/")
    }
    //开始运行
    ssc.start()
    //计算完毕退出
    try {
      ssc.awaitTermination()
    } catch {
      case NonFatal(e) =>
        log.error(s"[SparkStreamingFlume] Catch NonFatal Exception: ${e.getMessage}.")
        ssc.stop(stopSparkContext = true, stopGracefully = true)
    } finally {
      sc.stop()
    }
  }
}

package com.notalk.flink.demo

import com.notalk.flink.demo.common.LogSupport
import com.notalk.flink.demo.event.LoginEvent
import org.apache.flink.cep.scala.pattern.Pattern
import org.apache.flink.cep.scala.{CEP, PatternStream}
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.{KeyedStream, StreamExecutionEnvironment, createTypeInformation}
import org.apache.flink.streaming.api.windowing.time.Time
/**
 * Author: biyu.huang
 * Date: 2023/7/27 14:38
 * Description:
 */
object FlinkCEPDemo extends LogSupport {
  def main(args: Array[String]): Unit = {
    logger.info("start to run Flink CEP demo ... ")
    val scalaEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    scalaEnv.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    scalaEnv.setParallelism(1)

    val stream: KeyedStream[LoginEvent, String] = scalaEnv
      .fromElements(
        LoginEvent("user_1", "192.168.0.1", "fail", 2000L),
        LoginEvent("user_1", "192.168.0.2", "fail", 3000L),
        LoginEvent("user_1", "192.168.0.3", "fail", 4000L),
        LoginEvent("user_2", "192.168.10.10", "success", 5000L)
      )
      .assignAscendingTimestamps(_.eventTime)
      .keyBy(r => r.userId)

    val pattern: Pattern[LoginEvent, LoginEvent] = Pattern
      .begin[LoginEvent]("first")
      .where(r => r.eventType.equals("fail"))
      .next("second")
      .where(r => r.eventType.equals("fail"))
      .next("third")
      .where(r => r.eventType.equals("fail"))
      .within(Time.seconds(5))

    val patternedStream: PatternStream[LoginEvent] = CEP.pattern(stream, pattern)

    patternedStream
      .select((pattern: scala.collection.Map[String, Iterable[LoginEvent]]) => {
        val first = pattern("first").iterator.next()
        val second = pattern("second").iterator.next()
        val third = pattern("third").iterator.next()

        (first.userId, first.ip, second.ip, third.ip)
      })
      .print()

    scalaEnv.execute()
  }
}

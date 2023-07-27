package com.notalk.flink.demo

import com.notalk.flink.demo.common.LogSupport
import com.notalk.flink.demo.event.LoginEvent
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy
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
        LoginEvent("user_2", "192.168.10.10", "fail", 5000L),
        LoginEvent("user_2", "192.168.10.11", "fail", 6000L),
        LoginEvent("user_2", "192.168.10.12", "fail", 9000L),
        LoginEvent("user_3", "192.168.19.3", "fail", 10000L),
        LoginEvent("user_3", "192.168.19.4", "fail", 30000L),
        LoginEvent("user_3", "192.168.19.5", "success", 35000L),
        LoginEvent("user_4", "192.168.19.15", "success", 50000L),
        LoginEvent("user_5", "192.168.21.112", "fail", 51000L),
        LoginEvent("user_5", "192.168.23.13", "fail", 52000L),
        LoginEvent("user_5", "192.168.34.12", "fail", 53000L),
        LoginEvent("user_5", "192.168.44.11", "fail", 54000L),
      )
      .assignAscendingTimestamps(_.eventTime)
      .keyBy(r => r.userId)

    stream.print("login_event ")

    val threeTimesFailPattern: Pattern[LoginEvent, LoginEvent] = Pattern
      .begin[LoginEvent]("first")
      .where(r => r.eventType.equals("fail"))
      .next("second")
      .where(r => r.eventType.equals("fail"))
      .next("third")
      .where(r => r.eventType.equals("fail"))
      .within(Time.seconds(5))
    val failedStream: PatternStream[LoginEvent] = CEP.pattern(stream, threeTimesFailPattern)
    failedStream
      .select((pattern: scala.collection.Map[String, Iterable[LoginEvent]]) => {
        val first = pattern("first").iterator.next()
        val second = pattern("second").iterator.next()
        val third = pattern("third").iterator.next()

        (first.userId, first.ip, second.ip, third.ip)
      })
      .printToErr("fail_result ")

    val successPattern: Pattern[LoginEvent, LoginEvent] = Pattern
      .begin[LoginEvent]("fail", AfterMatchSkipStrategy.skipPastLastEvent()) // skipToFirst("success")
      .optional
      .where(r => r.eventType.equals("fail"))
      .followedBy("success")
      .where(r => r.eventType.equals("success"))
      .within(Time.seconds(30))
    val successStream = CEP.pattern(stream, successPattern)
    successStream.select((pattern: scala.collection.Map[String, Iterable[LoginEvent]]) => {
      val fail = if (pattern.contains("fail")) {
        pattern("fail").iterator.next()
      } else {
        LoginEvent("", "", "", 0L)
      }
      val success = pattern("success").iterator.next()

      (success.userId, fail.ip, success.ip)
    })
      .printToErr("success_result")

    scalaEnv.execute()
    logger.info("stop to run Flink CEP demo")
  }
}

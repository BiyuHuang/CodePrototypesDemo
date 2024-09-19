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
 * Description: A demo of Flink CEP to detect patterns in a stream of login events
 */
object FlinkCEPDemo extends LogSupport {
  def main(args: Array[String]): Unit = {
    logger.info("Start running Flink CEP demo...")

    // Set up the Flink execution environment
    val scalaEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    scalaEnv.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    scalaEnv.setParallelism(1)

    // Define the stream of login events
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
      .keyBy(_.userId)

    // Print the login events
    stream.print("login_event")

    // Define the pattern for three consecutive failed login attempts
    val threeTimesFailPattern: Pattern[LoginEvent, LoginEvent] = Pattern
      .begin[LoginEvent]("first")
      .where(_.eventType == "fail")
      .next("second")
      .where(_.eventType == "fail")
      .next("third")
      .where(_.eventType == "fail")
      .within(Time.seconds(5))

    // Apply the pattern to the stream and select the matching events
    val failedStream: PatternStream[LoginEvent] = CEP.pattern(stream, threeTimesFailPattern)
    failedStream
      .select((pattern: scala.collection.Map[String, Iterable[LoginEvent]]) => {
        val first = pattern("first").iterator.next()
        val second = pattern("second").iterator.next()
        val third = pattern("third").iterator.next()

        (first.userId, first.ip, second.ip, third.ip)
      })
      .printToErr("fail_result")

    // Define the pattern for a successful login following a failed attempt
    val successPattern: Pattern[LoginEvent, LoginEvent] = Pattern
      .begin[LoginEvent]("fail", AfterMatchSkipStrategy.skipPastLastEvent())
      .optional
      .where(_.eventType == "fail")
      .followedBy("success")
      .where(_.eventType == "success")
      .within(Time.seconds(30))

    // Apply the pattern to the stream and select the matching events
    val successStream = CEP.pattern(stream, successPattern)
    successStream.select((pattern: scala.collection.Map[String, Iterable[LoginEvent]]) => {
      val iterator: Iterator[LoginEvent] = pattern.getOrElse("fail", Iterable.empty).iterator
      val fail: LoginEvent = if (iterator.hasNext) iterator.next() else LoginEvent("", "", "", 0L)
      val success: LoginEvent = pattern("success").iterator.next()

      (success.userId, fail.ip, success.ip)
    })
      .printToErr("success_result")

    // Execute the Flink job
    scalaEnv.execute()

    logger.info("Stop running Flink CEP demo")
  }
}
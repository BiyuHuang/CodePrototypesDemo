package com.wallace.demo.app.utils

import java.sql.Timestamp

import com.wallace.demo.app.UnitSpec
import com.wallace.demo.app.common.LogSupport
import org.scalatest.FunSuite

/**
  * Created by 10192057 on 2018/5/9 0009.
  */
class DateTimeUtilsUnitSpec extends FunSuite with LogSupport {
  test("timestamp and us") {
    val now: Timestamp = new Timestamp(System.currentTimeMillis())
    now.setNanos(1000)
    val ns = DateTimeUtils.fromJavaTimestamp(now)
    log.info(s"Input: $now, Output: $ns")
    assert(ns % 1000000L === 1)
    assert(DateTimeUtils.toJavaTimestamp(ns) === now)

    List(-111111111111L, -1L, 0, 1L, 111111111111L).foreach { t =>
      val ts = DateTimeUtils.toJavaTimestamp(t)

      log.info(s"Input: $t, Output: $ts")
      assert(DateTimeUtils.fromJavaTimestamp(ts) === t)
      assert(DateTimeUtils.toJavaTimestamp(DateTimeUtils.fromJavaTimestamp(ts)) === ts)
    }
  }
}

/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.utils

import java.sql.Timestamp

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

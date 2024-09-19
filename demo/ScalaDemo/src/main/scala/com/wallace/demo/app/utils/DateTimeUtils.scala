package com.wallace.demo.app.utils

import java.sql.Timestamp

/**
  * Created by 10192057 on 2018/5/9 0009.
  */
object DateTimeUtils {
  type SQLTimestamp = Long
  final val MICROS_PER_MILLIS = 1000L
  final val MICROS_PER_SECOND = MICROS_PER_MILLIS * MILLIS_PER_SECOND
  final val MILLIS_PER_SECOND = 1000L

  def fromJavaTimestamp(t: Timestamp): SQLTimestamp = {
    if (t != null) {
      t.getTime() * 1000L + (t.getNanos().toLong / 1000) % 1000L
    } else {
      0L
    }
  }

  def toJavaTimestamp(us: SQLTimestamp): Timestamp = {
    // setNanos() will overwrite the millisecond part, so the milliseconds should be
    // cut off at seconds
    var seconds = us / MICROS_PER_SECOND
    var micros = us % MICROS_PER_SECOND
    // setNanos() can not accept negative value
    if (micros < 0) {
      micros += MICROS_PER_SECOND
      seconds -= 1
    }
    val t = new Timestamp(seconds * 1000)
    t.setNanos(micros.toInt * 1000)
    t
  }
}

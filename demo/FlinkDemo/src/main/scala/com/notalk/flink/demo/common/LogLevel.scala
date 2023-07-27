package com.notalk.flink.demo.common

/**
 * Author: biyu.huang
 * Date: 2023/7/27 12:09
 * Description:
 */
object LogLevel extends Enumeration {
  type LogLevel = Value
  val DEBUG, INFO, WARN, ERROR, TRACE = Value
}

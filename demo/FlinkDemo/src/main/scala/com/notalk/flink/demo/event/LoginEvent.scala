package com.notalk.flink.demo.event

/**
 * Author: biyu.huang
 * Date: 2023/7/27 14:39
 * Description:
 */
case class LoginEvent(
  userId: String,
  ip: String,
  eventType: String,
  eventTime: Long)

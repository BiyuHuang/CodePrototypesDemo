package com.notalk.flink.demo.common

import com.notalk.flink.demo.common.LogLevel.LogLevel
import com.typesafe.scalalogging.LazyLogging

import scala.reflect.ClassTag

/**
 * Author: biyu.huang
 * Date: 2023/7/27 12:08
 * Description:
 */
trait LogSupport extends LazyLogging {
  protected def logRecord[T: ClassTag](msg: T, level: LogLevel = LogLevel.INFO): Unit = {
    level match {
      case LogLevel.DEBUG => logger.debug(s"$msg")
      case LogLevel.INFO => logger.info(s"$msg")
      case LogLevel.WARN => logger.warn(s"$msg")
      case LogLevel.ERROR => logger.error(s"$msg")
      case LogLevel.TRACE => logger.trace(s"$msg")
      case _ => logger.info(s"$msg")
    }
  }
}

package com.wallace.demo.app.common

import com.typesafe.scalalogging.LazyLogging
import com.wallace.demo.app.common.LogLevel.LogLevel

import scala.reflect.ClassTag


/**
 * Created by Wallace on 2017/1/11.
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

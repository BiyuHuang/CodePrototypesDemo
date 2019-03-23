package com.wallace.demo.app.common

import com.wallace.demo.app.common.LogLevel.LogLevel
import org.slf4j.{Logger, LoggerFactory}

import scala.reflect.ClassTag


/**
  * Created by Wallace on 2017/1/11.
  */
trait LogSupport extends Serializable {
  val log: Logger = LoggerFactory.getLogger(this.getClass.getName.stripSuffix("$"))

  protected def logRecord[T: ClassTag](msg: T, level: LogLevel = LogLevel.INFO): Unit = {
    level match {
      case LogLevel.DEBUG => log.debug(s"$msg")
      case LogLevel.INFO => log.info(s"$msg")
      case LogLevel.WARN => log.warn(s"$msg")
      case LogLevel.ERROR => log.error(s"$msg")
      case LogLevel.TRACE => log.trace(s"$msg")
      case _ => log.info(s"$msg")
    }
  }
}

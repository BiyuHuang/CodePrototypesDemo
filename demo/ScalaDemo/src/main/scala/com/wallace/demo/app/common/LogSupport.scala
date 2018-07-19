package com.wallace.demo.app.common

import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by Wallace on 2017/1/11.
  */
trait LogSupport extends Serializable {
  protected val log: Logger = LoggerFactory.getLogger(this.getClass.getName.stripSuffix("$"))
}

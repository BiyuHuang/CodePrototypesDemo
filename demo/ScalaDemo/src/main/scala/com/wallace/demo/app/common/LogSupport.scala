package com.wallace.demo.app.common

import org.slf4j.LoggerFactory

/**
  * Created by Wallace on 2017/1/11.
  */
trait LogSupport {
  protected val log = LoggerFactory.getLogger(this.getClass)
}

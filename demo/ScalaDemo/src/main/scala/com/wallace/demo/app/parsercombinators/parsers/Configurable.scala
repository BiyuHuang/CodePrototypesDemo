package com.wallace.demo.app.parsercombinators.parsers

import com.wallace.demo.app.common.MethodMetaData

/**
  * Created by 10192057 on 2018/4/12 0012.
  */
trait Configurable {
  def configure(context: MethodContext): Unit
}

case class MethodContext(key: String, Value: MethodMetaData)
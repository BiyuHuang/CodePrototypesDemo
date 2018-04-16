package com.wallace.demo.app.parsercombinators.parsers

import com.wallace.demo.app.common.{LogSupport, MethodMetaData}

/**
  * Created by 10192057 on 2018/4/12 0012.
  */
trait Configurable extends LogSupport {
  /**
    * @param context MethodContext
    **/
  def configure(context: MethodContext): Unit
}

case class MethodContext(methodKey: String, methodMetaData: MethodMetaData)
package com.wallace.demo.app.parsercombinators.parsers

import java.util

import com.wallace.demo.app.common.MethodMetaData

/**
  * Created by 10192057 on 2018/4/12 0012.
  */
trait Configurable {
  /**
    * @param context          MethodContext
    * @param srcColumnsFields Raw data header
    **/
  def configure(context: MethodContext, srcColumnsFields: util.HashMap[String, Int]): Unit
}

case class MethodContext(methodKey: String, methodMetaData: MethodMetaData)
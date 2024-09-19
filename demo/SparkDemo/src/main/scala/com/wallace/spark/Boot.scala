package com.wallace.spark

import com.wallace.common.LogSupport

/**
  * Created by huangbiyu on 16-6-7.
  */
object Boot extends LogSupport {
  def main(args: Array[String]): Unit = {
    val a: StringBuffer = new StringBuffer
    a.append("wallace").append(",").append("hello").append("!")
    log.info(a.toString)
  }
}

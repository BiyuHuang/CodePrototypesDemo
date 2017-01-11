package com.wallace.demo.app

import com.wallace.demo.app.common.LogSupport

/**
  * Created by Wallace on 2017/1/11.
  */
object StringFuncUtils extends App with LogSupport {
  util.Properties.setProp("scala.time", "true")
  val str = """1,2,3,4,"a=1,b=2,c=3","e=1.2,f=32.1,g=1.3",7,8,9"""

  log.info(str)
}

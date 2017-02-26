package com.wallace.demo.app

import com.wallace.demo.app.common.LogSupport

/**
  * Created by Wallace on 2017/2/24.
  */
object Boot extends App with LogSupport {
  util.Properties.setProp("scala.time", "true")
}

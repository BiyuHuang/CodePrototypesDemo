package com.wallace.demo.app.DynamicLoadingConfigTest

import com.wallace.demo.app.common.ProjectConfig

/**
  * Created by Wallace on 2017/2/24.
  */
object DynamicLoadingConfig extends App with ProjectConfig {
  util.Properties.setProp("scala.time", "true")
  val prefixPath: (String) => String = (path: String) => path.replaceAll("\\\\", "/")
  var cnt = 0
  while (cnt < 10) {
    setConfigFiles(prefixPath("test.conf"))
    val test = config.getInt("test_1")
    cnt += 1

    log.info(s"$cnt ##### test = $test")
    config = null
    Thread.sleep(10000)
  }
}

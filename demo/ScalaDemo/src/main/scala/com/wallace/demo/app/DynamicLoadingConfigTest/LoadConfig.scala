package com.wallace.demo.app.DynamicLoadingConfigTest

import java.util.{Timer, TimerTask}

import com.typesafe.config.Config
import com.wallace.demo.app.common.ProjectConfig

/**
  * Created by Wallace on 2017/2/24.
  */
class LoadConfig extends TimerTask with ProjectConfig {
  var conf: Config = _
  val prefixPath = (path: String) => path.replaceAll("\\", "/")

  override def run(): Unit = {
    setConfigFiles(prefixPath("test.conf"))
    conf = config
  }

  def main(args: Array[String]): Unit = {
    util.Properties.setProp("scala.time", "true")
    val timer = new Timer
    var cnt = 0
    while (cnt < 10) {
      timer.schedule(new LoadConfig, 6000, 10000)
      val test = conf.getInt("test")

      log.info(s"@@@@@@@ $test")
      Thread.sleep(10000)
      cnt += 1
    }
  }
}

object LoadConfig {
  def apply: LoadConfig = new LoadConfig()
}
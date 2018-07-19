/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.dynamicloadconfig

import java.io.{File, FileInputStream}
import java.util.{Properties, Timer, TimerTask}

import com.typesafe.config.ConfigFactory
import com.wallace.demo.app.common.LogSupport
import com.wallace.demo.app.utils.SystemEnvUtils

import scala.util.control.NonFatal

/**
  * Created by wallace on 2017/10/28.
  */
class DynamicLoadConfig(props: Properties, prefixPath: String, configFiles: String*) extends TimerTask with LogSupport {
  //TODO 使用better file 监控文件的修改
  private val projectRootPath = SystemEnvUtils.getUserDir

  private val fileSep = SystemEnvUtils.getFileSeparator

  override def run(): Unit = {
    configFiles.foreach {
      fileName =>
        try {
          val filePath: String = getConfigHomePath + fileSep + fileName
          val file: File = new File(filePath)
          val in = new FileInputStream(file)
          props.load(in)
          in.close()
        } catch {
          case NonFatal(e) =>
            log.error(s"[${this.getClass.getCanonicalName}] Failed to reload $fileName: ${e.printStackTrace()}.")
        }
    }
  }

  private def getConfigHomePath: String = if (prefixPath.nonEmpty) s"$projectRootPath$fileSep$prefixPath" else s"$projectRootPath"
}

object DynamicLoadConfig extends LogSupport {
  def main(args: Array[String]): Unit = {
    util.Properties.setProp("scala.time", "true")
    val props: Properties = new Properties()
    val timer = new Timer
    timer.schedule(new DynamicLoadConfig(props, "conf", "test.conf", "test1.conf"), 1000L, 10000L)
    val eConf = ConfigFactory.empty("")
    println("###### " + eConf.toString)
    var cnt: Int = 0
    while (cnt < 10) {
      Thread.sleep(10000L)
      val conf1 = props.getProperty("conf1", "default_value")
      val conf2 = props.getProperty("conf2", "default_value")
      log.info(s"[${this.getClass.getSimpleName}]<$cnt> conf1: $conf1, conf2: $conf2.")
      cnt += 1
    }
  }
}

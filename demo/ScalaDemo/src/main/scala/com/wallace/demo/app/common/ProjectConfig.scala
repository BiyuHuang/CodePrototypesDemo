package com.wallace.demo.app.common

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by Wallace on 2017/2/24.
  */
trait ProjectConfig extends LogSupport {
  var config: Config = _

  /**
    * Default Setting Path
    */
  protected val configHome = "../vmax-conf/"

  def setConfigFiles(files: String*): Unit = synchronized {
    logger.debug(s"config home: $configHome")
    config = files.toList.map(load).reduce((a, b) => a.withFallback(b))
  }

  protected def load(file: String): Config = {
    val resourceFile = file
    val configFile = new File(makePath(file))
    if (configFile.exists()) {
      logger.debug(s"Loading file [${configFile.getPath}] and resource [$resourceFile]")
      ConfigFactory.parseFile(configFile).withFallback(ConfigFactory.load(resourceFile))
    } else {
      logger.debug(s"Loading resource [$resourceFile]")
      ConfigFactory.load(resourceFile)
    }
  }

  protected def makePath(filename: String): String = {
    val newDir = configHome.trim.replaceAll("""\\""", "/")
    if (newDir.endsWith("/")) configHome + filename else configHome + "/" + filename
  }
}

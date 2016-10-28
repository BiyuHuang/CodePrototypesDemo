package com.wallace.spark.common


import java.io.File
import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by huangbiyu on 16-6-4.
  */
trait ProjConfig extends LogSupport {
  var config: Config = null

  /**
    * Default Setting Path
    */
  protected val configHome = "../vmax-conf/"

  def setConfigFiles(files: String*): Unit = synchronized {
    log.debug(s"config home: $configHome")
    config = files.toList.map(load).reduce((a, b) => a.withFallback(b))
  }

  protected def load(file: String): Config = {
    val resourceFile = file
    val configFile = new File(makePath(file))
    if (configFile.exists()) {
      log.debug(s"Loading file [${configFile.getPath}] and resource [$resourceFile]")
      ConfigFactory.parseFile(configFile).withFallback(ConfigFactory.load(resourceFile))
    } else {
      log.debug(s"Loading resource [$resourceFile]")
      ConfigFactory.load(resourceFile)
    }
  }

  protected def makePath(filename: String): String = {
    val newDir = configHome.trim.replaceAll("""\\""", "/")
    if (newDir.endsWith("/")) configHome + filename
    else configHome + "/" + filename
  }
}

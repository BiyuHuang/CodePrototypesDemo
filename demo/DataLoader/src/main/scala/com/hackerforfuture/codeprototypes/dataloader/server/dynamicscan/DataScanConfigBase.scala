/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.server.dynamicscan

import java.io.File

import com.hackerforfuture.codeprototypes.dataloader.common.LogSupport
import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by wallace on 2018/1/20.
  */
trait DataScanConfigBase extends LogSupport {

  protected var configHome = "./demo-conf/"

  protected def makePath(filename: String): String = {
    val newDir = configHome.trim.replaceAll("""\\""", "/")
    if (newDir.endsWith("/")) configHome + filename else configHome + "/" + filename
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

  def loadScanConfig(files: Array[String]): Config = {
    files.map(load).reduce((a, b) => a.withFallback(b))
  }

  def loadScanConfig(files: Array[File]): Config = {
    loadScanConfig(files.map(_.getName))
  }

  def configFilePattern: String

  protected val getAllConfigFiles: Array[File] = if (configFilePattern.nonEmpty) {
    new File(configHome).listFiles().filter(_.getName.matches(configFilePattern))
  } else {
    Array.empty
  }

  protected val lastModifiedMap: Map[String, Long] = {
    if (getAllConfigFiles.nonEmpty) {
      getAllConfigFiles.map {
        file =>
          val fileName = file.getName
          val lastModified = file.lastModified()
          (fileName, lastModified)
      }.toMap
    } else {
      logger.warn("Does any upload config file exist?")
      Map.empty
    }
  }

  protected val getAllChangedConfigFiles: Array[File] = {
    getAllConfigFiles.map {
      file =>
        val fileName = file.getName
        val oldLastModified: Long = lastModifiedMap.apply(fileName)
        val newLastModified: Long = file.lastModified()

        if (newLastModified == oldLastModified) {
          logger.debug(s"$fileName never changed.")
          null
        } else {
          lastModifiedMap.updated(fileName, newLastModified)
          file
        }
    }.filter(_ != null)
  }
}

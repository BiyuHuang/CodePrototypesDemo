package com.hackerforfuture.codeprototypes.dataloader.common

import java.io.File

import com.hackerforfuture.codeprototypes.dataloader.DeveloperApi
import com.typesafe.config.Config

/**
  * com.hackerforfuture.codeprototypes.dataloader.common
  * Created by 10192057 on 2018/1/23 0023.
  */
case class PathInfo(canonicalPath: String, errorPath: String, tempDestPath: String, destPath: String) {
  override def toString: String = s"[CanonicalPath = $canonicalPath, ErrorPath = $errorPath, TempDestPath: $tempDestPath, DestPath: $destPath]"
}

@DeveloperApi
sealed trait Context extends Serializable {
  def file: File

  def fileName: String

  def targetKey: String

  def handleMode: String

  def pathInfo: PathInfo
}

case class FileContext(file: File, fileName: String, targetKey: String, handleMode: String, pathInfo: PathInfo) extends Context {
  // Handle File Records
  var nHandleRecords: Long = 0L

  // Handle File Size
  var nHandleBytes: Long = 0L

  override def toString: String =
    s"""
       |[FileContext] FileName: $fileName, TargetKey: $targetKey, HandleMode: $handleMode, PathInfo: ${pathInfo.toString}
       """.stripMargin
}

case class PluginFileContext(file: File, fileName: String, targetKey: String, handleMode: String, pathInfo: PathInfo, pluginArgs: Config)
  extends Context

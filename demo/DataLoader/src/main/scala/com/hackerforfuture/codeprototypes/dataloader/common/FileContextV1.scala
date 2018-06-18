package com.hackerforfuture.codeprototypes.dataloader.common

import java.io.File

import com.hackerforfuture.codeprototypes.dataloader.DeveloperApi
import com.hackerforfuture.codeprototypes.dataloader.common.DataType.DataType
import com.hackerforfuture.codeprototypes.dataloader.common.PersistMode.PersistMode
import com.typesafe.config.Config

import scala.collection.immutable.HashMap

/**
  * com.hackerforfuture.codeprototypes.dataloader.common
  * Created by 10192057 on 2018/1/23 0023.
  */

@DeveloperApi
sealed trait Source extends Serializable {
  def sType: DataType

  def sKey: String

  def sMode: PersistMode

  def sConf: SourceConfiguration
}

@DeveloperApi
sealed trait Persist extends Serializable {
  def pType: DataType

  def pKey: String

  def pMode: PersistMode

  def pConf: PersistConfiguration
}

@DeveloperApi
sealed trait ContextV2 extends Serializable {
  def source: Source

  def persist: Persist
}

@DeveloperApi
sealed trait Configuration extends Serializable

case class SourceConfiguration(sKey: String, sConfItemsMap: HashMap[String, AnyRef]) extends Configuration

case class PersistConfiguration(pKey: String, pConfItemsMap: HashMap[String, AnyRef]) extends Configuration

case class FileSource(sType: DataType = DataType.file, sKey: String, sMode: PersistMode, sConf: SourceConfiguration) extends Source

case class FilePersist(pType: DataType = DataType.file, pKey: String, pMode: PersistMode, pConf: PersistConfiguration) extends Persist

case class FilesContextV2(source: FileSource, persist: FilePersist) extends ContextV2


case class PathInfo(canonicalPath: String, errorPath: String, tempDestPath: String, destPath: String) {
  override def toString: String = s"[CanonicalPath = $canonicalPath, ErrorPath = $errorPath, TempDestPath: $tempDestPath, DestPath: $destPath]"
}

@DeveloperApi
sealed trait ContextV1 extends Serializable {
  def file: File

  def fileName: String

  def targetKey: String

  def handleMode: String

  //def pathInfo: PathInfo
}


abstract class ContextV1WithPath extends ContextV1 {
  def pathInfo: PathInfo
}

case class FileContextV1(file: File, fileName: String, targetKey: String, handleMode: String, pathInfo: PathInfo) extends ContextV1WithPath {
  // Handle File Records
  var nHandleRecords: Long = 0L

  // Handle File Size
  var nHandleBytes: Long = 0L

  override def toString: String =
    s"""
       |[FileContext] FileName: $fileName, TargetKey: $targetKey, HandleMode: $handleMode, PathInfo: ${pathInfo.toString}
       """.stripMargin
}

case class PluginFileContextV1(file: File, fileName: String, targetKey: String, handleMode: String, pathInfo: PathInfo, pluginArgs: Config)
  extends ContextV1
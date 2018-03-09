package com.hackerforfuture.codeprototypes.dataloader.Utils

import java.io.File
import java.util

import com.hackerforfuture.codeprototypes.dataloader.common.{FileContext, PathInfo}

import scala.util.matching.Regex

/**
  * com.hackerforfuture.codeprototypes.dataloader.Utils
  * Created by 10192057 on 2018/1/23 0023.
  */
object LoaderUtils {
  // TODO Recursive List Files
  def recursiveListFiles(rootFile: File): Array[File] = {
    if (rootFile.isFile) {
      Array(rootFile)
    } else {
      rootFile.listFiles().flatMap(recursiveListFiles)
    }
  }

  def getTimestamp(file: File): String = {
    val fileName = file.getName
    val regex: Regex ="""\d{8,14}""".r
    val temp: Option[Regex.Match] = regex.findFirstMatchIn(fileName)
    if (temp.isDefined) {
      val timeStamp: String = temp.get.matched
      if (timeStamp.length > 14) {
        timeStamp.substring(0, 14)
      } else {
        timeStamp.padTo(14, "0").toString()
      }
    } else {
      file.lastModified().toString
    }
  }

  def genFileContext(file: File, sep: String = "/"): FileContext = {
    val fileName = file.getName
    val filePath = file.getPath.replaceAll("""\\""", sep)
    val fileParentPath = file.getParentFile.getParent.replaceAll("""\\""", sep)
    val errorPath = fileParentPath + sep + "errorfiles"
    val tempPath = fileParentPath.split("destpath=").last
    val pathDetailInfo = tempPath.split(sep).toList
    val udfParams: util.HashMap[String, String] = new util.HashMap[String, String]
    pathDetailInfo match {
      // telecom_pm_cm/hdfs/zxvmax/telecom/temp/lte/rawdata/itg_pm_lte_enb_h_enb_d/p_provincecode=510000/
      case _ :: handleMode :: tail =>
        val destPath = sep + tail.mkString(sep).replace("target=", "") + sep
        val tempDestPath = "/temp/dataloader" + destPath
        tail.filter(_.contains("=")).foreach {
          elem =>
            val partVal = elem.split("=")
            udfParams.put(partVal.head, partVal.last)
        }

        val tgtName: Option[String] = Some(udfParams.getOrDefault("target", ""))
        val p_region = udfParams.getOrDefault("index", "")
        val targetKey = if (p_region.nonEmpty) tgtName.get + "_" + p_region else tgtName.get
        val pathInfo: PathInfo = PathInfo(filePath,
          errorPath,
          tempDestPath,
          destPath)
        FileContext(file, fileName, targetKey, handleMode, pathInfo)
      case _ =>
        FileContext(file, file.getName, "", "", PathInfo(filePath, errorPath, "", ""))
    }
  }
}

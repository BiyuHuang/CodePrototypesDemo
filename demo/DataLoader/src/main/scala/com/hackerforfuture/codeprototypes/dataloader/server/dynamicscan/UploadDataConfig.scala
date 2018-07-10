/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.server.dynamicscan

import com.typesafe.config.Config

/**
  * Created by wallace on 2018/1/20.
  */
class UploadDataConfig extends DataScanConfigBase {

  //configFilePattern = "uploadconfig.*.xml"

  def initUploadDataConf(): Config = {
    loadScanConfig(getAllConfigFiles)
  }

  def updateLoadDataConf(): Config = {
    if (getAllChangedConfigFiles.nonEmpty) {
      loadScanConfig(getAllChangedConfigFiles)
    } else {
      initUploadDataConf()
    }
  }

  override def configFilePattern: String = "uploadconfig.*.xml"
}

/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.utils

import java.util.Properties

import com.jcraft.jsch.{ChannelSftp, JSch}
import com.wallace.demo.app.common.Using

import scala.util.{Failure, Success, Try}

/**
  * Created by wallace on 2018/7/5.
  */
case class FtpMetaData(hostIp: String, port: Int, userName: String, passWord: String, timeOut: Int, ftpType: String = "sftp")

class FtpUtils(ftpMetadata: FtpMetaData) extends Using {

  /**
    * 登陆SFTP服务器
    *
    * @return boolean
    */
  def createSftpChannel(): Option[ChannelSftp] = {
    Try(new JSch().getSession(ftpMetadata.userName, ftpMetadata.hostIp, ftpMetadata.port)).flatMap {
      session =>
        if (ftpMetadata.passWord != null) session.setPassword(ftpMetadata.passWord)
        val config: Properties = new Properties()
        config.put("StrictHostKeyChecking", "no")
        session.setConfig(config)
        session.setTimeout(ftpMetadata.timeOut)
        log.debug("sftp session connected")
        log.debug("opening channel")
        Try {
          session.connect()
          val channel = session.openChannel(ftpMetadata.ftpType).asInstanceOf[ChannelSftp]
          channel.connect()
          channel
        }
    }
  } match {
    case Success(ch) => Some(ch)
    case Failure(e) =>
      log.error("Failed to login sftp server", e)
      None
  }
}


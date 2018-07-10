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
import org.apache.commons.net.ftp.{FTP, FTPClient}

import scala.util.{Failure, Success, Try}

/**
  * Created by wallace on 2018/7/5.
  */
case class FtpMetaData(hostIp: String, port: Int, userName: String, passWord: String, timeOut: Int = 6000, ftpType: String = "sftp")

class FtpUtils(ftpMetadata: FtpMetaData) extends Using {

  /** 规避多线程并发不断开问题 */
  private val sftpLocal: ThreadLocal[FtpUtils] = new ThreadLocal[FtpUtils]()

  /**
    * 连接SFTP服务器
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

  def createFtpClient(): Option[FTPClient] = {
    Try(new FTPClient()).flatMap {
      client =>
        Try {
          client.connect(ftpMetadata.hostIp, ftpMetadata.port)
          client.setControlEncoding("UTF-8")
          client.login(ftpMetadata.userName, ftpMetadata.passWord)
        }.flatMap {
          _ =>
            Try {
              client.setFileTransferMode(FTP.BINARY_FILE_TYPE)
              //client.listFiles().foreach(file => (file.getName, file.isFile, file.getTimestamp))
              client
            }
        }
    } match {
      case Success(client) => Some(client)
      case Failure(e) =>
        log.error("Failed to create FTP client", e)
        None
    }
  }
}

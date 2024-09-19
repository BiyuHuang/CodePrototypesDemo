/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.utils

import java.net.NetworkInterface
import java.util
import java.util.Properties

import scala.collection.JavaConverters._
import scala.util.matching.Regex

/**
  * Created by wallace on 2017/10/28.
  */
case class InetAddress(hostName: String, ipAddress: String)

object SystemEnvUtils {
  private val _env: util.Map[String, String] = System.getenv()
  private val _props: Properties = System.getProperties

  def getProps: Properties = this._props

  def getEnvs: util.Map[String, String] = this._env

  def getHostNameAndIPAddr: Array[InetAddress] = {
    val winReg: Regex = """^([W|w]indows).*""".r
    val linuxReg: Regex = """^([L|l]inux).*""".r
    getPropsByKey("os.name").toLowerCase match {
      case winReg(v) =>
        val hostName: String = java.net.InetAddress.getLocalHost.getHostName
        val ipAddr: String = java.net.InetAddress.getLocalHost.getHostAddress
        Array(InetAddress(hostName, ipAddr))
      case linuxReg(v) =>
        NetworkInterface.getNetworkInterfaces.asScala.flatMap {
          netInterface =>
            netInterface.getInetAddresses.asScala.map {
              elem =>
                val hostName: String = elem.getHostName
                val ipAddr: String = elem.getHostAddress
                elem.isSiteLocalAddress
                InetAddress(hostName, ipAddr)
            }
        }.toArray
      case _ => Array(InetAddress("localhost", "127.0.0.1"))
    }
  }

  def getUserDir: String = _props.getProperty("user.dir")

  def getFileSeparator: String = _props.getProperty("file.separator")

  def getEnvByKey(key: String): String = _env.get(key)

  def getPropsByKey(key: String): String = _props.getProperty(key)
}

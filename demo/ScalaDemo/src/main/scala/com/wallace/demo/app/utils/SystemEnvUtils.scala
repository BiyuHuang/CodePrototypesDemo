/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.utils

import java.util
import java.util.Properties

/**
  * Created by wallace on 2017/10/28.
  */
object SystemEnvUtils {
  private val _env: util.Map[String, String] = System.getenv()
  private val _props: Properties = System.getProperties

  def getUserDir: String = _props.getProperty("user.dir")

  def getFileSeparator: String = _props.getProperty("file.separator")

  def getEnvByKey(key: String): String = _env.get(key)

  def getPropsByKey(key: String): String = _props.getProperty(key)
}

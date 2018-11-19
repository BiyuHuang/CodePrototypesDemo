/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.common

import java.io.IOException

import scala.language.reflectiveCalls
import scala.util.control.NonFatal

/**
  * Created by Wallace on 2016/11/3.
  */
trait Using extends LogSupport {
  protected def using[A <: {def close() : Unit}, B](param: A)(f: A => B): B = {
    try {
      f(param)
    } finally {
      param.close()
    }
  }

  def tryOrIOException[T](block: => T): T = {
    try {
      block
    } catch {
      case e: IOException =>
        log.error("Exception encountered", e)
        throw e
      case NonFatal(e) =>
        log.error("Exception encountered", e)
        throw new IOException(e)
    }
  }

  def tryCatchFinally[A <: {def stop() : Unit}](param: A, errMsg: String = "")(f: A => Unit): Unit = {
    try {
      f(param)
    } catch {
      case NonFatal(e) =>
        log.error(errMsg, e)
    } finally {
      param.stop()
    }
  }
}

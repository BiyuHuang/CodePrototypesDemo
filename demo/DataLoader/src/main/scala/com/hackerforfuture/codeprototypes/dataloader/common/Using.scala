/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.common

import scala.util.control.NonFatal

/**
  * Created by wallace on 2018/1/20.
  */
trait Using extends LogSupport {
  protected def usingWithErrMsg[A <: {def close() : Unit}, B](param: A, errMsg: String)(f: A => B): Unit = {
    try {
      f(param)
    } catch {
      case NonFatal(e) =>
        log.error(s"$errMsg: ", e)
    } finally {
      param.close()
    }
  }

  protected def using[P <: {def close() : Unit}, R](param: P)(f: P => R): R = {
    try {
      f(param)
    } finally {
      param.close()
    }
  }
}

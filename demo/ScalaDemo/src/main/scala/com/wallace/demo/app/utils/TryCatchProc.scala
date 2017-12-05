/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.utils

import java.io.File

import com.wallace.demo.app.common.Using

import scala.util.control.NonFatal

/**
  * Created by wallace on 2017/12/4.
  */
trait TryCatchProc extends Using {
  def catchAndLogging[A, B](in: A, msg: String)(proc: File => B): Option[B] = {
    try {
      Some(proc(in))
    } catch {
      case NonFatal(e) =>
        log.error(s"""$msg, ${e.printStackTrace()}""".stripMargin)
        None
    }
  }
}

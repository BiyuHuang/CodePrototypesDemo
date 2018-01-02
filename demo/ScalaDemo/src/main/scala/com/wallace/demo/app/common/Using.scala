package com.wallace.demo.app.common

import com.wallace.demo.app.utils.FuncRuntimeDur

import scala.util.control.NonFatal

trait Using extends FuncRuntimeDur {
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

  protected def using[A <: {def close() : Unit}, B](param: A)(f: A => B): B = {
    try {
      f(param)
    } finally {
      param.close()
    }
  }
}

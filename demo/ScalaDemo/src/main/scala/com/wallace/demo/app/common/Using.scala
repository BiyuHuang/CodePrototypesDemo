package com.wallace.demo.app.common

import com.wallace.demo.app.utils.FuncRuntimeDur

import scala.language.reflectiveCalls
import scala.util.control.NonFatal

trait Using extends FuncRuntimeDur {
  protected def usingWithErrMsg[C <: {def close(): Unit}, R](ctx: C, errMsg: String)(func: C => R): Unit = {
    try {
      func(ctx)
    } catch {
      case NonFatal(e) =>
        logger.error(s"$errMsg: ", e)
    } finally {
      ctx.close()
    }
  }

  protected def using[C <: {def close(): Unit}, R](ctx: C)(func: C => R): R = {
    try {
      func(ctx)
    } finally {
      ctx.close()
    }
  }
}

trait Formatter[T] {
  def toString(obj: T): String

  def fromString(sObj: String): Option[T]
}
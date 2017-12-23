package com.wallace.common

import java.io.IOException

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
}

package com.wallace.common

/**
  * Created by Wallace on 2016/11/3.
  */
trait Using extends LogSupport {
  protected def using[A <: {def close() : Unit}, B](param: A)(f: A => B) = {
    try {
      f(param)
    } finally {
      param.close()
    }
  }
}

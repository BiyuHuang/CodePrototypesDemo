package com.wallace.demo.app.common

import com.wallace.demo.app.utils.FuncRuntimeDur

trait Using extends FuncRuntimeDur {
  protected def using[A <: {def close() : Unit}, B](param: A)(f: A => B): B = {
    try {
      f(param)
    } finally {
      param.close()
    }
  }
}

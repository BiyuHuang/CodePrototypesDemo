package com.wallace.demo.app.utils

import com.wallace.demo.app.common.LogSupport

/**
  * Created by Wallace on 2017/1/14.
  */

trait FuncRuntimeDur extends LogSupport {
  private val DEFAULT_PRECISION: Double = 0.000001

  /**
    * @param code  Code block/ Methods
    * @param times Execute code times
    * @return res: Execute code cost time, unit <ms>
    **/
  def runtimeDuration[T <: Any](code: => T, times: Int = 1): Double = {
    val startTime = System.nanoTime() * DEFAULT_PRECISION
    (0 until times).foreach(_ => code)
    val endTime = System.nanoTime() * DEFAULT_PRECISION
    val res = endTime - startTime
    res
  }
}

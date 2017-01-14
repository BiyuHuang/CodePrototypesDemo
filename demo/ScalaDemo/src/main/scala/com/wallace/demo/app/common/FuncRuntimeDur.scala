package com.wallace.demo.app.common

/**
  * Created by Wallace on 2017/1/14.
  */

trait FuncRuntimeDur {
  /**
    * @param code  Code block/ Methods
    * @param times Execute code times
    * @return res: Execute code cost time, unit <ms>
    **/
  def runtimeDuration[T <: Any](code: => T, times: Int = 1): Double = {
    val startTime = System.nanoTime() * 0.000001
    for (i <- 1 to times) {
      code
    }
    val endTime = System.nanoTime() * 0.000001
    val res = endTime - startTime
    res
  }
}

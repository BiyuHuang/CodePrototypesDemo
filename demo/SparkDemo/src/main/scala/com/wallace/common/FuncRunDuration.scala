package com.wallace.common

/**
  * Created by Wallace on 2016/10/30.
  * 统计执行 Code 代码的时间，单位：ms
  */
trait FuncRunDuration {
  /**
    * @param code  Code block/ Methods
    * @param times Execute code times
    * @return res: Execute code cost time, unit <ms>
    **/
  def executeDuration[T <: Any](code: => T, times: Int = 1): Double = {
    val startTime = System.nanoTime() * 0.000001
    for (_ <- 1 to times) {
      code
    }
    val endTime = System.nanoTime() * 0.000001
    val res = endTime - startTime
    res
  }
}

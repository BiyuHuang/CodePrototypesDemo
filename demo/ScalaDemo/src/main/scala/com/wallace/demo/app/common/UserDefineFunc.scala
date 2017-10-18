package com.wallace.demo.app.common

/**
  * Created by Wallace on 2016/11/6.
  */
trait UserDefineFunc {
  def toBigInt(x: Int): BigInt = {
    math.BigInt.int2bigInt(x)
  }
}

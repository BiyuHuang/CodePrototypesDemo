package com.wallace.demo.app.prototype.factorypatterndemo

/**
  * Created by Wallace on 2017/4/15.
  */
abstract class Operation[+T] {
  protected val numberA: T
  protected val numberB: T

  def calcResult: Option[T]
}

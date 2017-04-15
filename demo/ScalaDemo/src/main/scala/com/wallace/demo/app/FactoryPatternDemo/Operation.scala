package com.wallace.demo.app.FactoryPatternDemo

/**
  * Created by Wallace on 2017/4/15.
  */
trait Operation[+T] {
  protected val numberA: T
  protected val numberB: T

  def calcResult: Option[T]
}

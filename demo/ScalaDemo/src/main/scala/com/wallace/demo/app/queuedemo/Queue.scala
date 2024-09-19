package com.wallace.demo.app.queuedemo

import scala.collection.mutable.ArrayBuffer

/**
  * Created by 10192057 on 2018/6/4 0004.
  */
sealed trait Queue[T] {
  def get(): T

  def put(x: T): Unit
}

abstract class IntQueue extends Queue[Int] {
  //abstract override def put(x: Int): Unit = super.put(x * 2)
  protected val buf: ArrayBuffer[Int] = new scala.collection.mutable.ArrayBuffer[Int]()
}

abstract class DoubleQueue extends Queue[Double] {
  protected val buf: ArrayBuffer[Double] = new scala.collection.mutable.ArrayBuffer[Double]()
}
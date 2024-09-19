package com.wallace.demo.app.patterns.iteratordemo

/**
 * Author: biyu.huang
 * Date: 2024/5/13 18:54
 * Description:
 */
trait Iterator[T] {
  def hasNext: Boolean

  def next(): T
}

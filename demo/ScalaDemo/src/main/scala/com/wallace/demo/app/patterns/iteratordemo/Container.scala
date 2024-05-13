package com.wallace.demo.app.patterns.iteratordemo

/**
 * Author: biyu.huang
 * Date: 2024/5/13 18:55
 * Description:
 */
trait Container[T] {
  def createIterator(): Iterator[T]
}

package com.wallace.demo.app.patterns.iteratordemo

/**
 * Author: biyu.huang
 * Date: 2024/5/13 18:56
 * Description:
 */
class NameRepository(elements: List[String]) extends Container[String] {
  override def createIterator(): Iterator[String] = new NameIterator

  private class NameIterator extends Iterator[String] {
    private var index: Int = 0

    override def hasNext: Boolean = {
      if (index < elements.length) true else false
    }

    override def next(): String = {
      if (hasNext) {
        val value: String = elements(index)
        index += 1
        value
      } else {
        null
      }
    }
  }
}

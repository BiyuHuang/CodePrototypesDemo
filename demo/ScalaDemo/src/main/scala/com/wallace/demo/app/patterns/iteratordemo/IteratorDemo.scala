package com.wallace.demo.app.patterns.iteratordemo

import com.typesafe.scalalogging.LazyLogging

/**
 * Author: biyu.huang
 * Date: 2024/5/13 18:54
 * Description:
 */
object IteratorDemo extends LazyLogging {
  def main(args: Array[String]): Unit = {
    val elements = List("aaaa", "bbbb", "cccc", "dddd", "eeee")
    val nameRepository = new NameRepository(elements)
    val iter = nameRepository.createIterator()
    while (iter.hasNext) {
      logger.info("current element -> %s".format(iter.next()))
    }
  }
}

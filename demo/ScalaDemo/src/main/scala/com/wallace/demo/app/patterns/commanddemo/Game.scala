package com.wallace.demo.app.patterns.commanddemo

import com.typesafe.scalalogging.LazyLogging

/**
 * Author: biyu.huang
 * Date: 2024/5/13 16:53
 * Description:
 */
class Game(name: String) extends LazyLogging {
  def close(): Unit = {
    logger.info("[ts=%s] %s has been closed.".format(System.currentTimeMillis(), name))
  }

  def publish(): Unit = {
    logger.info("[ts=%s] %s has been published.".format(System.currentTimeMillis(), name))
  }
}

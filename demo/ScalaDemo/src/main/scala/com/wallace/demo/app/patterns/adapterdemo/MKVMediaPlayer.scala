package com.wallace.demo.app.patterns.adapterdemo

import com.typesafe.scalalogging.LazyLogging

/**
 * Author: biyu.huang
 * Date: 2024/5/13 15:59
 * Description:
 */
class MKVMediaPlayer extends LazyLogging {
  def playMKV(fileName: String): Unit = {
    logger.info("Media Type -> MKV, Media File -> %s".format(fileName))
  }
}

package com.wallace.demo.app.patterns.adapterdemo

import com.typesafe.scalalogging.LazyLogging

/**
 * Author: biyu.huang
 * Date: 2024/5/13 15:47
 * Description:
 */
class VLCMediaPlayer extends LazyLogging {
  def playVLC(fileName: String): Unit = {
    logger.info("Media Type -> VLC, Media File -> %s".format(fileName))
  }
}

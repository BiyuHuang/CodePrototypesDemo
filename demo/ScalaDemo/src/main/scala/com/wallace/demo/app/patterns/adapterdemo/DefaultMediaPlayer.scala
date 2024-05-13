package com.wallace.demo.app.patterns.adapterdemo

/**
 * Author: biyu.huang
 * Date: 2024/5/13 15:36
 * Description:
 */
class DefaultMediaPlayer() extends MediaPlayer {
  override def playMp4(fileName: String): Unit = {
    logger.info("Media Type -> MP4, Media File -> %s".format(fileName))
  }
}

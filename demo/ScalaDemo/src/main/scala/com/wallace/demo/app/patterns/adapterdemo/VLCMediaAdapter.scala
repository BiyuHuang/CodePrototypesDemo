package com.wallace.demo.app.patterns.adapterdemo

/**
 * Author: biyu.huang
 * Date: 2024/5/13 15:35
 * Description:
 */
class VLCMediaAdapter extends Adapter {
  private val vlcMediaPlayer = new VLCMediaPlayer

  override def playMp4(fileName: String): Unit = {
    this.vlcMediaPlayer.playVLC(fileName)
  }
}

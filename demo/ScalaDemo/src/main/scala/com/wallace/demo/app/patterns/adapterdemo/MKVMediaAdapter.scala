package com.wallace.demo.app.patterns.adapterdemo

/**
 * Author: biyu.huang
 * Date: 2024/5/13 15:58
 * Description:
 */
class MKVMediaAdapter extends Adapter {
  private val mkvMediaPlayer = new MKVMediaPlayer

  override def playMp4(fileName: String): Unit = {
    mkvMediaPlayer.playMKV(fileName)
  }
}

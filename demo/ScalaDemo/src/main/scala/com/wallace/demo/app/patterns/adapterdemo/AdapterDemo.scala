package com.wallace.demo.app.patterns.adapterdemo

/**
 * Author: biyu.huang
 * Date: 2024/5/13 15:50
 * Description: Adapter Pattern(适配器模式)
 */
object AdapterDemo {
  def main(args: Array[String]): Unit = {
    val defaultMediaPlayer: MediaPlayer = new DefaultMediaPlayer
    defaultMediaPlayer.playMp4("test1.mp4")

    val vlcMediaAdapter: MediaPlayer = new VLCMediaAdapter
    vlcMediaAdapter.playMp4("test2.vlc")

    val mkvMediaAdapter: MediaPlayer = new MKVMediaAdapter
    mkvMediaAdapter.playMp4("test3.mkv")
  }
}

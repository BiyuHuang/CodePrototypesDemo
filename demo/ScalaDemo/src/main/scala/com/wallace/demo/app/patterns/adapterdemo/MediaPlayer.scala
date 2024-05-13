package com.wallace.demo.app.patterns.adapterdemo

import com.typesafe.scalalogging.LazyLogging

/**
 * Author: biyu.huang
 * Date: 2024/5/13 15:34
 * Description:
 */
trait MediaPlayer extends LazyLogging {
  def playMp4(fileName: String): Unit
}

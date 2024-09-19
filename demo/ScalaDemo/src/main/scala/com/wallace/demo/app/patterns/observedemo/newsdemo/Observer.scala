package com.wallace.demo.app.patterns.observedemo.newsdemo

import com.typesafe.scalalogging.LazyLogging

/**
 * Author: biyu.huang
 * Date: 2024/4/8 17:22
 * Description: trait of observer
 */
trait Observer extends LazyLogging {
  def notify(news: String): Unit
}

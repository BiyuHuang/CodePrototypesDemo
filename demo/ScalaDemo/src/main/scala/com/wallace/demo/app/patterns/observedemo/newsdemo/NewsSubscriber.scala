package com.wallace.demo.app.patterns.observedemo.newsdemo

import java.util.concurrent.LinkedBlockingQueue
import scala.reflect.runtime.universe._


/**
 * Author: biyu.huang
 * Date: 2024/4/8 17:30
 * Description:
 */
class NewsSubscriber(name: String) extends Observer {
  private final val newsQueue: LinkedBlockingQueue[String] = new LinkedBlockingQueue[String]()

  override def notify(news: String): Unit = {
    logger.info("%s received news -> %s".format(name, news))
    newsQueue.put(news)
  }
}

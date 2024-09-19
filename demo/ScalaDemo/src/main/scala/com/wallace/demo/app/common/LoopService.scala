package com.wallace.demo.app.common

import java.util.concurrent.CountDownLatch
import scala.util.control.NonFatal

/**
 * Author: biyu.huang
 * Date: 2022/12/23 17:53
 * Description:
 */
sealed trait Service {
  def init(): Unit = {
    // Won't do anything by default. But user can override it.
  }

  def start(): Unit

  def stop(): Unit
}

abstract class LoopService(sleepMills: Long, name: String) extends Service with LogSupport {
  private final val countDown: CountDownLatch = new CountDownLatch(1)

  private val thread = new Thread(new LoopThread(), "%s-thread".format(name))

  override def start(): Unit = {
    logger.info("starting %s-thread".format(name))
    this.thread.setDaemon(true)
    this.thread.start()
    logger.info("started %s-thread".format(name))
  }

  def handleWork(): Unit

  override def stop(): Unit = {
    this.countDown.countDown()
  }

  class LoopThread extends Runnable {
    override def run(): Unit = {
      while (countDown.getCount > 0) {
        try {
          handleWork()
          Thread.sleep(sleepMills)
        } catch {
          case NonFatal(e) =>
            logger.error(e.getMessage, e)
            countDown.countDown()
        }
      }
    }
  }
}

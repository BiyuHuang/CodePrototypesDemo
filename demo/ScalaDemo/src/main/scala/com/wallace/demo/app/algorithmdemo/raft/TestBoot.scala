package com.wallace.demo.app.algorithmdemo.raft

import com.wallace.demo.app.common.LogSupport

import java.util.concurrent.CountDownLatch
import scala.util.control.NonFatal

/**
 * Author: biyu.huang
 * Date: 2022/12/23 18:31
 * Description:
 */
object TestBoot extends LogSupport {
  private val countDownLatch = new CountDownLatch(1)

  def main(args: Array[String]): Unit = {
    try {
      Runtime.getRuntime.addShutdownHook(new Thread {
        override def run(): Unit = {
          logger.info("shutdown now ...")
          countDownLatch.countDown()
        }
      })

      val node1: NodeCoordinator = NodeCoordinator(5000L, "node1")
      node1.init()
      node1.start()
    } catch {
      case NonFatal(_) =>
        countDownLatch.countDown()
    }
    countDownLatch.await()
  }
}

package com.wallace.demo.app.multithread

import java.util.concurrent.{ExecutorService, Executors}

import com.wallace.demo.app.common.LogSupport

/**
  * com.wallace.demo.app.multithread
  * Created by 10192057 on 2017/10/18 0018.
  */
object MultiThreadTestDemo extends LogSupport {
  private val maxPoolSize: Int = Runtime.getRuntime.availableProcessors()
  private val currentPoolSize: Int = Math.min(maxPoolSize, 5)

  def main(args: Array[String]): Unit = {
    //创建线程池
    log.info(s"Thread Pool Size: $currentPoolSize")
    val threadPool: ExecutorService = Executors.newFixedThreadPool(currentPoolSize)
    try {
      //提交5个线程
      for (i <- 1 to 5) {
        //threadPool.submit(new ThreadDemo("thread"+i))
        threadPool.execute(new ThreadDemo("thread" + i))
      }
    } finally {
      threadPool.shutdown()
    }
  }

  //定义线程类，每打印一次睡眠100毫秒
  class ThreadDemo(threadName: String) extends Runnable {
    override def run(): Unit = {
      val threadId = Thread.currentThread().getId
      for (i <- 1 to 10) {
        log.info(threadName + "|" + threadId)
        Thread.sleep(100)
      }
      val state: Thread.State = Thread.currentThread().getState
      val symbol: Boolean = Thread.currentThread().isAlive
      log.info(s"Thread State: $state, Thread Symbol: $symbol, Thread Name: $threadName, Thread ID: $threadId")
    }
  }

}
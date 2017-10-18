package com.wallace.demo.app.multithread

import java.util.concurrent.{ExecutorService, Executors}

import com.wallace.demo.app.common.LogSupport

/**
  * com.wallace.demo.app.multithread
  * Created by 10192057 on 2017/10/18 0018.
  */
object MultiThreadTestDemo extends LogSupport {
  def main(args: Array[String]): Unit = {
    //创建线程池
    val threadPool: ExecutorService = Executors.newFixedThreadPool(5)
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
      for (i <- 1 to 10) {
        log.info(threadName + "|" + i)
        Thread.sleep(100)
      }
    }
  }

}
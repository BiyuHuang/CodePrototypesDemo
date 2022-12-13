package com.wallace.demo.app.asyncdemo

import com.wallace.demo.app.common.Using

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Created by 10192057 on 2018/3/15.
  */
object AsyncDemo extends Using {
  private val value: Future[String] = Future {
    Thread.currentThread().getName
  }


  def main(args: Array[String]): Unit = {
    value.onComplete {
      case Success(res) => logger.info(s"[OnComplete] $res")
      case Failure(e) => logger.info(s"[OnComplete] $e")
    }

    Thread.sleep(100)

    value.value.get match {
      case Success(res) =>
        logger.info(s"[Match] $res")
      case Failure(e) =>
        logger.info(s"[Match] $e")
        throw e
    }


    logger.info("Shutting down...")
  }
}

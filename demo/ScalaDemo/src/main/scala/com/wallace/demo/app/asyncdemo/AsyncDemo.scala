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
    value onSuccess {
      case res => log.info(s"[OnSuccess] $res")
      case error => log.info(s"[OnSuccess] $error")
    }

    value.onComplete {
      case Success(res) => log.info(s"[OnComplete] $res")
      case Failure(e) => log.info(s"[OnComplete] $e")
    }

    Thread.sleep(100)

    value.value.get match {
      case Success(res) =>
        log.info(s"[Match] $res")
      case Failure(e) =>
        log.info(s"[Match] $e")
        throw e
    }


    log.info("Shutting down...")
  }
}

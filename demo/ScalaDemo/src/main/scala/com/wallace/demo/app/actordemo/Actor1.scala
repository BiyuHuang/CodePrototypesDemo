package com.wallace.demo.app.actordemo

import akka.actor.Actor
import com.wallace.demo.app.common.LogSupport

/**
  * Created by 10192057 on 2017/9/12.
  */
class Actor1 extends Actor with LogSupport {
  override def receive: Receive = {
    case "test" => logger.info("received test.")
    case _ => logger.info("received unknown message.")
  }
}

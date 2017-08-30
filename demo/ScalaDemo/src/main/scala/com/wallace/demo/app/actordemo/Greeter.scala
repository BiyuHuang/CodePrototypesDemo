package com.wallace.demo.app.actordemo

import akka.actor.Actor
import com.wallace.demo.app.common.LogSupport

/**
  * Created by 10192057 on 2017/8/15.
  */
object Greeter {

  case object Greet

  case object Done

}

class Greeter extends Actor with LogSupport {
  def receive: PartialFunction[Any, Unit] = {
    case Greeter.Greet =>
      log.info("[Greeter] Hello World!")
      sender() ! Greeter.Done
  }
}

package com.wallace.demo.app.actordemo

import akka.actor.{ActorSystem, Props}
import com.wallace.demo.app.common.LogSupport

/**
  * Created by 10192057 on 2017/9/12.
  */
object Demo1 extends LogSupport {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("Demo1")
    val actor1 = system.actorOf(Props[Actor1], name = "Actor1")
    actor1 ! "test"
    actor1 ! "other"

  }
}

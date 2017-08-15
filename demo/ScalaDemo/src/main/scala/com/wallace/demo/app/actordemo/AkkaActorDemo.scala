package com.wallace.demo.app.actordemo

import akka.actor.{Actor, ActorRef, Props}
import com.wallace.demo.app.common.LogSupport

/**
  * Created by 10192057 on 2017/8/15.
  */
object AkkaActorDemo extends LogSupport {
  def main(args: Array[String]): Unit = {
    akka.Main.main(Array(classOf[HelloWorld].getName))
  }
}

class HelloWorld extends Actor {
  override def preStart(): Unit = {
    // create the greeter actor
    val greeter: ActorRef = context.actorOf(Props[Greeter], "greeter")
    // tell it to perform the greeting
    greeter ! Greeter.Greet
  }

  def receive: PartialFunction[Any, Unit] = {
    // when the greeter is done, stop this actor and with it the application
    case Greeter.Done => context.stop(self)
  }
}

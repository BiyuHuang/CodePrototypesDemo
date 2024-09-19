package com.wallace.demo.app.algorithmdemo.raft

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

import java.util.concurrent.ConcurrentHashMap

/**
 * Author: biyu.huang
 * Date: 2023/1/13 18:28
 * Description:
 */
trait ActorSystemSupport {
  protected val builder: ActorSystemBuilder = new ActorSystemBuilder()

  class ActorSystemBuilder() {
    private final val actorConfMap = new ConcurrentHashMap[String, String]()

    private var HOST: Option[String] = None
    private var PORT: Option[Int] = None
    private var ACTOR_SYSTEM_NAME: Option[String] = None

    def setHost(host: String): ActorSystemBuilder = {
      this.HOST = Option(host)
      this
    }

    def setPort(port: Int): ActorSystemBuilder = {
      this.PORT = Option(port)
      this
    }

    def setName(name: String): ActorSystemBuilder = {
      this.ACTOR_SYSTEM_NAME = Option(name)
      this
    }

    def setOption(key: String, value: String): ActorSystemBuilder = {
      this.actorConfMap.put(key, value)
      this
    }

    def build(): ActorSystem = {
      require(this.HOST.isDefined, "please set hostname for ActorSystem")
      require(this.PORT.isDefined, "please set port for ActorSystem")
      require(this.ACTOR_SYSTEM_NAME.isDefined, "please set name for ActorSystem")
      this.actorConfMap.put("akka.actor.provider", "akka.remote.RemoteActorRefProvider")
      this.actorConfMap.put("akka.remote.netty.tcp.hostname", this.HOST.get)
      this.actorConfMap.put("akka.remote.netty.tcp.port", s"${this.PORT.get}")
      this.actorConfMap.put("akka.actor.warn-about-java-serializer-usage", "false")
      ActorSystem.create(this.ACTOR_SYSTEM_NAME.get, ConfigFactory.parseMap(this.actorConfMap))
    }
  }
}


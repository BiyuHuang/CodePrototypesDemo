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

    private var host: Option[String] = None
    private var port: Option[Int] = None
    private var name: Option[String] = None

    def setHost(host: String): ActorSystemBuilder = {
      this.host = Option(host)
      this
    }

    def setPort(port: Int): ActorSystemBuilder = {
      this.port = Option(port)
      this
    }

    def setName(name: String): ActorSystemBuilder = {
      this.name = Option(name)
      this
    }

    def setOption(key: String, value: String): ActorSystemBuilder = {
      this.actorConfMap.put(key, value)
      this
    }

    def build(): ActorSystem = {
      require(this.host.isDefined, "please set hostname for ActorSystem")
      require(this.port.isDefined, "please set port for ActorSystem")
      require(this.name.isDefined, "please set name for ActorSystem")
      this.actorConfMap.put("akka.actor.provider", "akka.remote.RemoteActorRefProvider")
      this.actorConfMap.put("akka.remote.netty.tcp.hostname", this.host.get)
      this.actorConfMap.put("akka.remote.netty.tcp.port", s"${this.port.get}")
      this.actorConfMap.put("akka.actor.warn-about-java-serializer-usage", "false")
      ActorSystem.create(this.name.get, ConfigFactory.parseMap(this.actorConfMap))
    }
  }
}

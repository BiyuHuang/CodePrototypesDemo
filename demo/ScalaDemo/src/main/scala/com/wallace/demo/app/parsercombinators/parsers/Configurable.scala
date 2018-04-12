package com.wallace.demo.app.parsercombinators.parsers

/**
  * Created by 10192057 on 2018/4/12 0012.
  */
trait Configurable {
  def configure(context: Context): Unit
}

case class Context(key: String, Value: Map[String, String])
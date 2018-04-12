package com.wallace.demo.app.parsercombinators.parsers

import java.util.Locale

import com.wallace.demo.app.common.ParserType

/**
  * Created by 10192057 on 2018/4/11 0011.
  */
object ParserBuilderFactory {

  private def lookUp(name: String): Option[Class[_ <: Parser]] = try {
    Option(ParserType.valueOf(name.toUpperCase(Locale.ENGLISH)).getBuilderClass)
  } catch {
    case _: IllegalArgumentException => None
  }

  def newInstance(name: String): Parser = {
    val clazz: Option[Class[_ <: Parser]] = lookUp(name)
    if (clazz.isDefined) {
      clazz.get.newInstance()
    } else {
      Class.forName(name).asInstanceOf[Parser]
    }
  }
}

package com.wallace.demo.app.parsercombinators.parsers

import java.util.Locale

import com.wallace.demo.app.common.ParserType

/**
  * Created by 10192057 on 2018/4/11 0011.
  */
object ParserFactory {
  def newInstance(name: String): AbstractParser = {
    name match {
      case MethodKeyType.default => new ExtractFieldsAbstractParser
      case MethodKeyType.concat => new ConcatFieldsAbstractParser
      case MethodKeyType.split => new SplitFieldsAbstractParser
    }
  }
}

object MethodKeyType {
  val default: String = "extract"
  val split: String = "split"
  val concat: String = "concat"
}

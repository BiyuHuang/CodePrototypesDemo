package com.wallace.demo.app.parsercombinators.parsers

/**
  * Created by 10192057 on 2018/4/11 0011.
  */
object ParserFactory {
  def newInstance(methodKey: String): AbstractParser = {
    methodKey match {
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

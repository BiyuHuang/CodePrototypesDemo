package com.wallace.demo.app.parsercombinators.parsers

/**
  * Created by 10192057 on 2018/4/11 0011.
  */
object ParserFactory {
  def newInstance(methodKey: String): AbstractParser = {
    methodKey match {
      case MethodKeyType.default => new ExtractFieldsParser
      case MethodKeyType.split => new SplitFieldsParser
      case MethodKeyType.concat => new ConcatFieldsParser
      case MethodKeyType.compute => new ComputeFieldsParser
      case MethodKeyType.replaceStr => new ReplaceStrFieldsParser
      case MethodKeyType.substring => new SubStringFieldsParser
      case MethodKeyType.addtimestamp => new AddTimeStampFieldsParser
      case _ => throw new IllegalArgumentException(s"MethodKey #$methodKey# is illegal.")
    }
  }
}

object MethodKeyType {
  val default: String = "extract"
  val split: String = "split"
  val concat: String = "concat"
  val compute: String = "compute"
  val replaceStr: String = "replacestring"
  val substring: String = "substring"
  val addtimestamp: String = "addtimestamp"
}

package com.wallace.demo.app.utils

import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.ForkJoinTaskSupport

/**
  * Created by Wallace on 2017/1/11.
  */
object StringFuncUtils extends FuncRuntimeDur {
  private var _uniqueIndex: Long = updateUniqueIndex(((System.currentTimeMillis() - 946656000000L) / 1000) % Int.MaxValue)
  private val _maxParallelism: Int = Runtime.getRuntime.availableProcessors()
  private val _curParallelism: Int = Math.min(_maxParallelism, 5)

  def splitString(str: String, fieldSeparator: String, specialChar: String): Array[String] = {
    val resultArr = new ArrayBuffer[String]()
    var temp = str
    str match {
      case v if v.isEmpty => Array("")
      case _ =>
        while (temp.nonEmpty) {
          var nPos: Int = temp.indexOf(fieldSeparator)
          if (nPos >= 0) {
            var sSub = temp.substring(0, nPos)
            if (sSub.indexOf(specialChar) != -1) {
              if (sSub.indexOf(specialChar) == sSub.lastIndexOf(specialChar)) {
                temp = temp.substring(nPos, temp.length)
                nPos = temp.indexOf(specialChar)
                if (nPos != -1) {
                  sSub = sSub + temp.substring(0, nPos)
                }
                sSub = sSub.replaceAll(specialChar, "")
                resultArr.append(sSub)
                temp = if (nPos + 2 < temp.length) temp.substring(nPos + 2, temp.length) else ""
              } else {
                temp = temp.substring(nPos + 1, temp.length)
                sSub = sSub.replaceAll(specialChar, "")
                resultArr.append(sSub)
              }
            } else {
              resultArr.append(sSub)
              temp = temp.substring(nPos + 1, temp.length)
            }
          } else {
            resultArr.append(temp)
            temp = temp.substring(temp.length)
          }
        }
        resultArr.result().toArray
    }
  }

  def main(args: Array[String]): Unit = {
    val pool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(_curParallelism))
    util.Properties.setProp("scala.time", "true")
    runtimeDuration({
      _uniqueIndex = updateUniqueIndex(_uniqueIndex)
      log.info(s"UniqueIndex: ${_uniqueIndex}")
    }, 10)

    log.info(formatString("16"))
    val str0 = """1,2,3,4,"a=1,b=2,c=3","e=1.2,f=32.1,g=1.3",7,8,9"""
    val str1 = """1,2,3,"4,"a=1,b=2,c=3",10,11,12,13,"e=1.2,f=32.1,g=1.3",7,8,9"""
    val str2 = """1,2,3,4","a=1,b=2,c=3",10,11,12,13,"e=1.2,f=32.1,g=1.3",7,8,9"""
    val input = Array(str0, str1, str2)
    input.par.tasksupport = pool
    input.par.foreach {
      str =>
        val threadName = Thread.currentThread().getName
        log.info(s"$threadName: $str")
        val res: Array[String] = splitString(str, ",", "\"")
        synchronized {
          res.foreach {
            elem =>
              log.info(s"$threadName: $elem")
          }
        }
    }
  }

  def updateUniqueIndex(initIndex: Long): Long = synchronized {
    if (initIndex > Int.MaxValue) 0L else initIndex + 1
  }

  def formatString(s: String): String = {
    //TODO scala字符串格式化-StringLike.format()
    s match {
      case "" => ""
      case _: String =>
        val a = "%1$s-%2$s-%3$s".format("scala", "StringLike", "format")
        val b = "%d%%".format(s.toInt)
        val c = "%8.3f".format(s.toDouble)
        val d = "%08.3f".format(s.toDouble)
        val e = "%09d".format(s.toInt)
        val f = "%.2f".format(s.toDouble)
        val g = f"${s.toDouble}%.2f"

        s"""
           |$a
           |$b
           |$c
           |$d
           |$e
           |$f
           |$g
         """.stripMargin
      case _ =>
        ""
    }
  }
}

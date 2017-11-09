package com.wallace.demo.app.utils

import com.wallace.demo.app.common.LogSupport

import scala.collection.mutable.ArrayBuffer

/**
  * Created by Wallace on 2017/1/11.
  */
object StringFuncUtils extends LogSupport {
  private var uniqueIndex: Long = updateUniqueIndex(((System.currentTimeMillis() - 946656000000L) / 1000) % Int.MaxValue)

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
                sSub = sSub + temp.substring(0, nPos)
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

    util.Properties.setProp("scala.time", "true")
    //val baseTimeMills: Long = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2000-01-01 00:00:00.000").getTime / 1000 //% 50000 + 50000
    //log.info("%15d".format((System.currentTimeMillis() - baseTimeMills) % Int.MaxValue))

    var cnt = 0L
    while (cnt < 10) {
      log.info(updateUniqueIndex(uniqueIndex).toString)
      Thread.sleep(10)
      cnt += 1
    }

    log.info(formatString("16"))
    val str = """1,2,3,4,"a=1,b=2,c=3","e=1.2,f=32.1,g=1.3",7,8,9"""

    log.info(str)
    val result: Array[String] = splitString(str, ",", "\"")
    for (elem <- result) {
      log.info("@@@@@ " + elem)
    }
  }

  def updateUniqueIndex(initIndex: Long): Long = synchronized {
    uniqueIndex = if (initIndex > Int.MaxValue) 0L else initIndex + 1
    uniqueIndex
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

package com.wallace.demo.app

import com.wallace.demo.app.common.LogSupport

import scala.collection.mutable.ArrayBuffer

/**
  * Created by Wallace on 2017/1/11.
  */
object StringFuncUtils extends LogSupport {
  def main(args: Array[String]): Unit = {
    util.Properties.setProp("scala.time", "true")
    val str = """1,2,3,4,"a=1,b=2,c=3","e=1.2,f=32.1,g=1.3",7,8,9"""

    log.info(str)
    val result: Array[String] = SplitString(str, ",", "\"")
    for (elem <- result) {
      log.info("@@@@@ " + elem)
    }
  }

  def SplitString(str: String, fieldSeparator: String, specialChar: String): Array[String] = {
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
}

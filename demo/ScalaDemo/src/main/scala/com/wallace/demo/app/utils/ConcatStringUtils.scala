package com.wallace.demo.app.utils

import com.wallace.demo.app.common.LogSupport

import scala.collection.mutable.ArrayBuffer

/**
  * Created by Wallace on 2017/2/8.
  */
object ConcatStringUtils extends LogSupport {
  def main(args: Array[String]) {
    val str = "a1,b2,c3,d4,e5,3,n1,n11,n12,n2,n21,n22,n3,n31,n32,f6,g8,h9"
    val temp = str.split(",", -1)
    val result = concatNColumn(temp, 5, 3)
    for (elem <- result) {
      log.info("######## " + elem)
    }
  }

  /** 支持连续多个可变长的字段 **/
  def concatNColumn(data: Array[String], index: Int, nLength: Int): Array[String] = {
    val nNum: Int = data(index).toInt
    val tailData = data.slice(index + 1 + nNum * 3, data.length)
    val tempResultData = new ArrayBuffer[String]()
    (0 to index).foreach {
      i =>
        tempResultData.append(data(i))
    }

    val nData = data.drop(index + 1).slice(0, nNum * 3)

    val n1_Data = new ArrayBuffer[String]()
    val n2_Data = new ArrayBuffer[String]()
    val n3_Data = new ArrayBuffer[String]()
    nData.indices.foreach {
      case i if i % nLength == 0 => n1_Data.append(nData(i))
      case i if i % nLength == 1 => n2_Data.append(nData(i))
      case i if i % nLength == 2 => n3_Data.append(nData(i))
    }
    val n1_Res = n1_Data.result.mkString("$")
    val n2_Res = n2_Data.result.mkString("$")
    val n3_Res = n3_Data.result.mkString("$")
    tempResultData.append(n1_Res)
    tempResultData.append(n2_Res)
    tempResultData.append(n3_Res)

    tempResultData.result().toArray ++ tailData
  }
}

package com.wallace.demo.app.utils

import com.wallace.demo.app.common.LogSupport

import scala.collection.mutable.ArrayBuffer

/**
  * Created by Wallace on 2017/2/8.
  */
object ConcatStringUtils extends LogSupport {

  def concatCols(data: String, fixedIndex: Int, nCellCols: Int): Array[String] = {
    val temp = data.split(",", -1)
    val nCellNum = temp(fixedIndex).toInt
    val headData: Array[String] = temp.take(fixedIndex + 1)
    val tailData: Array[String] = temp.slice(fixedIndex + 1 + nCellNum * nCellCols, temp.length)

    val nCellData: Array[String] = temp.slice(fixedIndex + 1, fixedIndex + 1 + nCellNum * nCellCols)
    val nCellResData = new ArrayBuffer[String]()

    (0 until nCellCols).foreach {
      x =>
        val nCellTempResData = new ArrayBuffer[String]()
        nCellData.indices.foreach {
          index =>
            if (index % nCellCols == x) {
              nCellTempResData.append(nCellData(index))
            } else {
              log.debug("[ConcatStringUtils]: No match value.")
            }
        }
        nCellResData.append(nCellTempResData.result().mkString("$"))
    }
    log.debug(s"[HeadData] ${headData.mkString("##")}")
    log.debug(s"[nCellData] ${nCellData.mkString("##")}")
    log.debug(s"[TailData] ${tailData.mkString("##")}")
    headData ++ nCellResData.result().toArray[String] ++ tailData
  }

  def main(args: Array[String]): Unit = {
    val str = "a1,b2,c3,d4,e5,4,n1,n11,n12,n13,n2,n21,n22,n23,n3,n31,n32,n33,n4,n41,n42,n43,f6,g8,h9"
    val temp = str.split(",", -1)
    val result = concatNColumn(temp, 5, 4) // a1,b2,c3,d4,e5,4,n1$n2$n3$n4,n11$n21$n31$n41,n12$n22$n32$n42,n13$n23$n33$n43,f6,g8,h9
    for (elem <- result) {
      log.info("######## " + elem)
    }
    log.info("@@@@@@ " + result.mkString(","))
  }

  /** 支持连续多个可变长的字段, 返回结果的字段个数是固定的. **/
  def concatNColumn(data: Array[String], index: Int, nLength: Int): Array[String] = {
    val nNum: Int = data(index).toInt
    val tailData = data.slice(index + 1 + nNum * nLength, data.length)
    val tempResultData = new ArrayBuffer[String]()
    (0 to index).foreach {
      i =>
        tempResultData.append(data(i))
    }

    val nData = data.drop(index + 1).slice(0, nNum * nLength)

    val n1Data = new ArrayBuffer[String]()
    val n2Data = new ArrayBuffer[String]()
    val n3Data = new ArrayBuffer[String]()
    val n4Data = new ArrayBuffer[String]()

    nData.indices.foreach {
      case i if i % nLength == 0 => n1Data.append(nData(i))
      case i if i % nLength == 1 => n2Data.append(nData(i))
      case i if i % nLength == 2 => n3Data.append(nData(i))
      case i if i % nLength == 3 => n4Data.append(nData(i))
    }
    val n1Res = n1Data.result.mkString("$")
    val n2Res = n2Data.result.mkString("$")
    val n3Res = n3Data.result.mkString("$")
    val n4Res = n4Data.result().mkString("$")

    tempResultData.append(n1Res)
    tempResultData.append(n2Res)
    tempResultData.append(n3Res)
    tempResultData.append(n4Res)

    tempResultData.result().toArray ++ tailData
  }
}

package com.wallace.demo.app.convertmrtonb

import java.util

import com.wallace.demo.app.common.Using
import com.wallace.demo.app.utils.FuncUtil

import scala.collection.JavaConverters._

object ConvertMRToNeighborCellDemo extends Using {

  def main(args: Array[String]): Unit = {
    val mrRecord: String = """119724139438609,649015,51,,167,,,573670410,-75,-13.5,6639,159352,104.50401996,31.32234264,2,13,,,,25,,10,1,-47209,-19592,4841,240,6,,755037673,,0,,,,3,,,,,,,,1825,2,1825$1825,649015$647346,49$49,165$126,-74$-78,-10.0$-14.5,1$1,,,,,,,,-2.46,1,755037673,20738,2,,,0,-74.0,165,"4841,-4721,-1960","4841,-2361,-980","4841,-945,-392","4841,-473,-196","4841,-237,-98",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,649015_51,,,,,,,,,,,,,,-473.0,-196.0,,,,,,,,,,,,,,,,,649015_49$647346_49"""
    val res = FuncUtil.ProcessNeighbourInfo(mrRecord)
    res.asScala.foreach {
      line =>
        log.info(line)
    }

    assert(args.length >= 1, "Please enter run times.")
    val times: Int = args(0).toInt
    val mrData = new util.Vector[String]()
    FuncUtil.GetSplitString(mrData, mrRecord, ",")
    testProcessNBDataByVector(mrData, times)
    testProcessNBDataByLine(mrRecord, times)
  }


  def testProcessNBDataByLine(line: String, times: Int): Unit = {
    var cnt: Int = 0
    val costTime: Double = runtimeDuration {
      while (cnt < times) {
        FuncUtil.ProcessNeighbourInfoOther(line)
        cnt += 1
      }
    }
    log.info(s"[testProcessNBDataByLine] RunTimes: $cnt, CostTime: $costTime ms.")
  }

  def testProcessNBDataByVector(data: util.Vector[String], times: Int): Unit = {
    var cnt: Int = 0
    val costTime: Double = runtimeDuration {
      while (cnt < times) {
        FuncUtil.ProcessNeighbourInfo(data)
        cnt += 1
      }
    }
    log.info(s"[testProcessNBDataByVector] RunTimes: $cnt, CostTime: $costTime ms.")
  }

}

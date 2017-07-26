package com.wallace.scala_demo.Collection

import com.wallace.common.LogSupport

/**
  * Created by Wallace on 2016/12/3.
  */
object CollectionDemo extends App with LogSupport {
  util.Properties.setProp("scala.time", "true")

  /**
    * flatMap
    **/
  val arr: Array[String] = Array("cl$ass", "a", "b").sortBy(_.length)
  arr.foreach(x => log.info("%f".formatted(x)))

  //Array("zzzzz", "eeeee$ffffff$gggggggg", "aaaaaaa$bbbbbbbbb$ccccccccc")
  val arrRes: Array[String] = arr.flatMap {
    x =>
      if (x.contains("$")) {
        val temp = x.split("\\$", -1)
        temp.map(x => "@" + x + "_" + x.reverse + "@")
      } else {
        Array(s"@${x}_${x.reverse}@")
      }
  }
  arrRes.foreach(elem => log.info("%f".formatted(elem)))


  /**
    * Partition
    **/
  val ls = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
  val resLs: (List[Int], List[Int]) = ls.partition(_ % 2 == 0)
  log.info(s"[CollectionDemo] res_Ls: ${resLs._1}, ${resLs._2}")


  def getWords(lines: Seq[String]): Seq[String] = lines flatMap (line => line split "\\W+")
}

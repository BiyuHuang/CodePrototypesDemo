/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.collection

import com.wallace.demo.app.common.LogSupport

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


  /**
    * Get all elems
    **/
  val testArr: Array[Int] = Array(1, 2, 3)
  val resArr: Array[Int] = Array(testArr: _*)
  log.info(s"TestArr: ${testArr.mkString(" ")}, ResArr: ${resArr.mkString(" ")}")

  def getWords(lines: Seq[String]): Seq[String] = lines flatMap (line => line split "\\W+")
}

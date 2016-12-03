package com.wallace.scala_demo.Collection

/**
  * Created by Wallace on 2016/12/3.
  */
object CollectionDemo extends App {
  util.Properties.setProp("scala.time", "true")

  /**
    * flatMap
    **/
  val arr: Array[String] = Array("a", "b", "cl$ass")
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
  for (elem <- arrRes) println(elem)


  /**
    * Partition
    **/
  val ls = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
  val resLs: (List[Int], List[Int]) = ls.partition(_ % 2 == 0)
  println(resLs._1, resLs._2)


  def getWords(lines: Seq[String]): Seq[String] = lines flatMap (line => line split "\\W+")
}

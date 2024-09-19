package com.wallace.demo.app.eulerpathdemo

import com.wallace.demo.app.common.LogSupport

import java.util.Scanner
import scala.jdk.CollectionConverters.collectionAsScalaIterableConverter
import scala.util.control.Breaks._

/**
 * com.wallace.demo.app.EulerPathDemo
 * Created by 10192057 on 2017/10/10 0010.
 */
object EulerPathBoot extends App with LogSupport {

  //  Sample Input
  //  3 3
  //  1 2
  //  1 3
  //  2 3
  //  3 2
  //  1 2
  //  2 3
  //  0
  //
  //  Sample Output
  //    1
  //  0
  util.Properties.setProp("scala.time", "true")
  val handler: EluerPathDemo = new EluerPathDemo
  val in = new Scanner(System.in)
  breakable {
    while (true) {
      val n = in.nextInt()
      n match {
        case 0 => break()
        case _ =>
          val m = in.nextInt()
          val degree: Array[Int] = new Array[Int](n)

          (0 until m).foreach {
            _ =>
              var temp: Int = 0
              val a = in.nextInt()
              val b = in.nextInt()
              val temp1 = new Array[Int](handler.MAX)
              temp1.update(b - 1, 1)
              handler.map.update(a - 1, temp1)
              val temp2 = new Array[Int](handler.MAX)
              temp2.update(a - 1, 1)
              handler.map.update(b - 1, temp2)
              temp += 1
              degree.update(a - 1, temp)
              degree.update(b - 1, temp)
          }
          if (handler.judge(degree) && handler.bfs(n)) {
            handler.result.add(1)
          } else {
            handler.result.add(0)
          }
      }
    }
  }

  for (elem <- handler.result.asScala) {
    logger.info(s"$elem")
  }
}

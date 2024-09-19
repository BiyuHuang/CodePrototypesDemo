package com.wallace.demo.app.utils

/**
 * Author: biyu.huang
 * Date: 2024/5/30 10:13
 * Description:
 */
object IDVerifier {
  val parameters: Array[Int] = Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2)

  val verifierCode: Array[Char] = Array('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2')

  def evaluate(id: String): Boolean = {
    val idList: List[String] = id.toList.map(_.toString)
    val tmp: Int = idList.take(17).zipWithIndex.map(x => x._1.toInt * parameters(x._2)).sum
    val code: Char = verifierCode(tmp % 11)
    code == id.last
  }

  def main(args: Array[String]): Unit = {
    evaluate("")
  }
}

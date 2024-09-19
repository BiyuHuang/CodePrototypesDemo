package com.wallace.demo.app.eulerpathdemo

import java.util

import com.wallace.demo.app.common.LogSupport

import scala.collection.mutable.ArrayBuffer

/**
  * com.wallace.demo.app.EulerPathDemo
  * Created by 10192057 on 2017/10/10 0010.
  * 一个无向图存在欧拉路当且仅当该图是连通的且有且只有2个点或0个点的度数是奇数,
  * 若有2个点的度数是奇数,则此时这两个点只能作为欧拉路径的起点和终点.
  */
class EluerPathDemo extends LogSupport {
  val MAX = 1000
  val map: Array[Array[Int]] = Array.ofDim[Int](MAX, MAX) //输入图(二维数组)
  val result: util.ArrayList[Integer] = new util.ArrayList[Integer] //用于存放最终输出结果

  def judge(degree: Array[Int]): Boolean = {
    //TODO 判断给定图的每个顶点的度是否均为偶数
    val temp: Array[Boolean] = degree.map {
      case v if v % 2 != 0 => false
      case _ => true
    }
    if (temp.contains(false)) false else true
  }

  def bfs(n: Int): Boolean = {
    val used: Array[Boolean] = new Array[Boolean](n)
    val list = new ArrayBuffer[Int]()
    list.append(0)
    used.update(0, true)
    while (list.nonEmpty) {
      val temp: Int = list.remove(0)
      (0 until n).foreach {
        elem =>
          if (!used.apply(elem) && map.apply(temp).apply(elem) != 0) {
            used.update(elem, true)
            list.append(elem)
          }
      }
    }
    val result = (0 until n).map {
      elem =>
        if (used.apply(elem)) true else false
    }
    if (result.contains(false)) false else true
  }
}

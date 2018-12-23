package com.wallace.demo.app.sortdemo

import com.wallace.demo.app.common.LogSupport

class BitMapDemo(var size: Int = 1000) extends LogSupport {
  //长度10000，可以存储10000*32的数字

  private val dataBitMap: Array[Int] = new Array[Int](size)

  //接下来，定义设置位的方法
  //i >> 5相当于i/32, i&0X1F相当于i%32
  def setBit(i: Int): Unit = {
    dataBitMap(i >> 5) |= (1 << (i & 0X1F))
  }

  //定义判断数字是否存在的方法
  def exists(i: Int): Boolean = {
    (dataBitMap(i >> 5) & (1 << (i & 0X1F))) != 0
  }

  // 最后，定义重置方法
  def reset(i: Int): Unit = {
    dataBitMap(i >> 5) &= (~(1 << (i & 0X1F)))
  }
}

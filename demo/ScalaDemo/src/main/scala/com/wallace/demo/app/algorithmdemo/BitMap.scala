package com.wallace.demo.app.algorithmdemo

/**
  * Created by wallace on 2019/10/28.
  */
class BitMap(size: Int) extends Cloneable with Serializable {
  private final val SIZE: Int = size
  private val BM: Array[Int] = new Array[Int](SIZE / 32 + 1)

  def insert(a: Int): Unit = {
    // e / 32 为十进制在数组BM中的下标
    val index = a >> 5
    // e % 32 为十进制在数据BM(index)中的下标
    BM(index) |= 1 << (a & 0x1F)
  }

  def exists(a: Int): Boolean = {
    val index = a >> 5
    ((BM(index) >> (a & 0x1F)) & 1) == 1
  }

  def getValue(a: Int): Int = {
    val tmp: Int = BM(a >> 5) >> (a & 0x1F)
    tmp & 1
  }


  def show(num: Int): Unit = {
    (0 until num).foreach {
      i =>
        val tmpVal: Array[Int] = new Array[Int](32)
        var temp = BM(i)
        tmpVal.indices.foreach {
          j =>
            tmpVal(j) |= (temp & 1)
            temp >>= 1
        }
        println("BM[" + i + "] = [" + tmpVal.mkString(", ") + "]")
    }
  }
}

object BitMap {
  def main(args: Array[String]): Unit = {
    val bMap = new BitMap(320)
    //    Array(1, 2, 3, 5, 8, 30, 32, 64, 56, 159, 120, 21, 17, 35, 45, 320).foreach(bMap.insert)
    Array(2, 3, 5, 8).foreach(bMap.insert)

    println(s">>> Key: 159, Value: ${bMap.getValue(159)}.")
    if (bMap.exists(320)) {
      println("Temp: 320 has already existed.")
    }
    bMap.show(6)
  }
}



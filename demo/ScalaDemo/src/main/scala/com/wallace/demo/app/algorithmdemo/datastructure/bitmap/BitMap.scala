package com.wallace.demo.app.algorithmdemo.datastructure.bitmap

import com.wallace.demo.app.common.LogSupport

/**
 * Created by wallace on 2019/10/28.
 */
class BitMap(size: Long) extends Cloneable with Serializable {
  private final val SIZE: Int = (size / 64 + 1).toInt
  assert(SIZE > 0, s"too large size: $size")
  private val BM: Array[Long] = new Array[Long](SIZE)

  def insert(e: Long): Unit = {
    // e / 32 为十进制在数组BM中的下标
    val index: Int = (e >> 6).toInt
    assert(index >= 0, s"$index($e >> 6) is greater than $SIZE")
    // e % 32 为十进制在数据BM(index)中的下标
    BM(index) |= 1L << (e & 0x3F)
  }

  def exists(a: Long): Boolean = getValue(a) == 1L

  def getValue(e: Long): Long = BM((e >> 6).toInt) >> (e & 0x3F) & 1

  def show(num: Int): Unit = {
    val end: Int = Math.min(num, BM.length)
    val formatStr: String = s"%0${end.toString.length}d"
    (0 until end).foreach {
      i =>
        val tmpVal: Array[Long] = new Array[Long](64)
        var temp: Long = BM(i)
        tmpVal.indices.foreach {
          j =>
            tmpVal(j) |= (temp & 1)
            temp >>= 1
        }
        println("BM[" + formatStr.format(i) + "] = [" + tmpVal.mkString(", ") + "]")
    }
  }

  def bitCount: Long = this.BM.map(x => java.lang.Long.bitCount(x).toLong).sum
}

object BitMap extends LogSupport {
  def main(args: Array[String]): Unit = {
    val bMap: BitMap = new BitMap(320L)
    Array(1, 2, 3, 5, 8, 30, 32, 64, 56, 159, 120, 21, 17, 35, 45, 320)
      .foreach(x => bMap.insert(x.toLong))
    Array(2, 3, 5, 8).foreach(x => bMap.insert(x.toLong))

    logger.info(s">>> Key: 159, Value: ${bMap.getValue(159L)}.")
    if (bMap.exists(320L)) {
      logger.info("Temp: 320 exists")
    }
    bMap.show(6)
  }
}



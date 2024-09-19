package com.wallace.demo.app.sortdemo

class BitMapDemo(bitSize: Int, granularity: Int) {
  def this() = this(8192, 32)

  val bitMap: BitMapBuilder = new BitMapBuilder()
    .setBitSize(bitSize)
    .setGranularity(granularity)
    .build()

  class BitMapBuilder {
    //i >> 5相当于i/32, i&0X1F相当于i%32
    private var bitSize: Int = 0
    private var granularity: Int = 32
    private var bitArray: Array[Int] = Array.empty
    private var shift: Int = 0
    private var digit: Int = 0

    def setBitSize(s: Int): BitMapBuilder = {
      require(s >= 0 & s <= Int.MaxValue,
        s"bitSize must be within the range:[1, Int.MaxValue], current value: $s")
      this.bitSize = s
      this
    }

    def setGranularity(g: Int): BitMapBuilder = {
      require(g > 0 & g <= 32, s"granularity must be within the range:(0, 32], current value: $g")
      this.granularity = g
      this
    }

    def build(): BitMapBuilder = {
      this.bitArray = new Array[Int]((bitSize + (granularity - 1)) / granularity)
      this.shift = granularity.toBinaryString.length - 1
      this.digit = (Math.pow(2, this.shift) - 1).toInt
      this
    }

    def setBit(i: Int): Unit = {
      this.bitArray(i >> this.shift) |= (1 << (i & this.digit))
    }

    //定义判断数字是否存在的方法
    def exists(i: Int): Boolean = {
      (this.bitArray(i >> this.shift) & (1 << (i & this.digit))) != 0
    }

    // 最后，定义重置方法
    def reset(i: Int): Unit = {
      this.bitArray(i >> this.shift) &= (~(1 << (i & this.digit)))
    }

    def bitCount(): Long = {
      this.bitArray.map(x => java.lang.Integer.bitCount(x)).sum
    }

    override def toString: String = {
      "BitMap(" + this.bitArray.mkString(",") + ")"
    }
  }
}

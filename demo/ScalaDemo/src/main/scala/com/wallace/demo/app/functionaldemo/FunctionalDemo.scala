package com.wallace.demo.app.functionaldemo

import com.wallace.demo.app.common.{LogSupport, UserDefineFunc}

/**
  * Created by Wallace on 2016/11/6.
  */
object FunctionalDemo extends UserDefineFunc with LogSupport {
  val p0: (Int, Int, Int) => Int = sum
  val p2: (Int) => Int = sum(10, _: Int, 20)
  val p3: (Int, Int) => Int = sum(_: Int, 100, _: Int)

  //    util.Properties.setProp("scala.time", "true")  //继承App, 统计运行时间
  def main(args: Array[String]): Unit = {
    val a: Int = 3
    val b: BigInt = toBigInt(a)
    log.info(s"${Int.MaxValue}, ${Int.MinValue}, ${b.pow(a)}")
    log.info(s"${p0(1, 2, 3)}") // 6
    log.info(s"${p2(100)}") // 130
    log.info(s"${p3(10, 1)}")
    log.info("[Partial Functions] " + divide(10))
    log.info("[Partial Functions] " + divide1(10))
    log.info("[Partial Functions] " + direction(180))
    log.info("[匿名函数] " + m1(2))
    log.info("[偏应用函数] " + sum(1, 2, 3))
    log.info("Curry 函数] " + curriedSum(5)(6))
  }

  /**
    * Scala-Partial Functions(偏函数)
    * 定义一个函数，而让它只接受和处理其参数定义域范围内的子集，对于这个参数范围外的参数则抛出异常，这样的函数就是偏函数
    **/
  def divide: PartialFunction[Int, Int] = new PartialFunction[Int, Int] {
    override def isDefinedAt(x: Int): Boolean = x != 0

    override def apply(v1: Int): Int = 100 / v1
  }

  def divide1: PartialFunction[Int, Int] = {
    case v if v != 0 => 100 / v
    case _ =>
      throw new IllegalArgumentException("[FunctionalDemo] Illegal Argument Exception: args shouldn't be zero.")
  }

  def direction: PartialFunction[Int, String] = {
    case v if math.abs(v / 90) == 0 => "East"
    case v if math.abs(v / 90) == 1 => "North"
    case v if math.abs(v / 90) == 2 => "West"
    case v if math.abs(v / 90) == 3 => "South"
    case _ => "East"
  }

  /**
    * Lambda表达式, 匿名函数
    **/
  def m1: (Int) => Int = (x: Int) => x * x

  /**
    * 偏应用函数
    **/
  def sum(a: Int, b: Int, c: Int): Int = a + b + c

  /**
    * Curry函数
    **/
  def curriedSum(x: Int)(y: Int): Int = x + y
}
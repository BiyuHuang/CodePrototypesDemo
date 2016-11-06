package com.wallace.scala_demo.FunctionalDemo

import com.wallace.common.{LogSupport, UserDefineFunc}

/**
  * Created by Wallace on 2016/11/6.
  */
object FunctionalDemo extends UserDefineFunc with LogSupport {
  //    util.Properties.setProp("scala.time", "true")  //继承App, 统计运行时间
  def main(args: Array[String]): Unit = {
    val a: Int = 3
    val b: BigInt = toBigInt(a)
    Console.println(Int.MaxValue, Int.MinValue, b.pow(a))
    Console.println("[Partial Functions] " + divide(10))
    Console.println("[Partial Functions] " + divide1(10))
    Console.println("[Partial Functions] " + direction(180))
    Console.println("[匿名函数] " + m1(2))
  }

  /**
    * Scala-Partial Functions(偏函数)
    * 定义一个函数，而让它只接受和处理其参数定义域范围内的子集，对于这个参数范围外的参数则抛出异常，这样的函数就是偏函数
    **/
  def divide = new PartialFunction[Int, Int] {
    override def isDefinedAt(x: Int): Boolean = x != 0

    override def apply(v1: Int): Int = 100 / v1
  }

  def divide1: PartialFunction[Int, Int] = {
    case v if v != 0 => 100 / v
  }

  def direction: PartialFunction[Int, String] = {
    case v if math.abs(v / 90) == 0 => "East"
    case v if math.abs(v / 90) == 1 => "North"
    case v if math.abs(v / 90) == 2 => "West"
    case v if math.abs(v / 90) == 3 => "South"
    case v if math.abs(v / 90) == 4 => "East"
  }

  /**
    * 偏应用函数
    **/
  def sum(a: Int, b: Int, c: Int) = a + b + c

  val p0 = sum _
  val p2 = sum(10, _: Int, 20)
  val p3 = sum(_: Int, 100, _: Int)

  p0(1, 2, 3) // 6
  p2(100) // 130
  p3(10, 1)

  // 111

  /**
    * Lambda表达式, 匿名函数
    **/
  def m1 = (x: Int) => x * x

  /**
    * Curry函数
    **/
  def curriedSum(x: Int)(y: Int) = x + y
}
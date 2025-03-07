package com.wallace.demo.app.functionaldemo

import com.wallace.demo.app.common.{LogSupport, UserDefineFunc}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

/**
 * Created by Wallace on 2016/11/6.
 */
object FunctionalDemo extends UserDefineFunc with LogSupport {
  val p0: (Int, Int, Int) => Int = sum
  val p2: (Int) => Int = sum(10, _: Int, 20)
  val p3: (Int, Int) => Int = sum(_: Int, 100, _: Int)

  //    util.Properties.setProp("scala.time", "true")  //继承App, 统计运行时间
  def main(args: Array[String]): Unit = {
    tryFlatMap("b")

    futureFlatMap("a")

    futureFlatMap("c")

    val a: Int = 3
    val b: BigInt = toBigInt(a)
    logger.info(s"${Int.MaxValue}, ${Int.MinValue}, ${b.pow(a)}")
    logger.info(s"${p0(1, 2, 3)}") // 6
    logger.info(s"${p2(100)}") // 130
    logger.info(s"${p3(10, 1)}")
    logger.info("[Partial Functions] " + divide(10))
    logger.info("[Partial Functions] " + divide1(10))
    logger.info("[Partial Functions] " + direction(180))
    logger.info("[匿名函数] " + m1(2))
    logger.info("[偏应用函数] " + sum(1, 2, 3))
    logger.info("Curry 函数] " + curriedSum(5)(6))
  }

  /**
   * Scala-Partial Functions(偏函数)
   * 定义一个函数，而让它只接受和处理其参数定义域范围内的子集，对于这个参数范围外的参数则抛出异常，这样的函数就是偏函数
   * */
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

  def tryFlatMap(key: String): Unit = {
    val m1: Map[String, Int] = Map("a" -> 1, "b" -> 2, "e" -> 5)
    val m2: Map[Int, String] = Map(1 -> "west", 2 -> "east", 3 -> "north", 4 -> "south")
    val m3: Map[String, String] = Map("west" -> "left", "east" -> "right", "north" -> "up", "south" -> "down")

    Try(m1(key)).flatMap(x => Try(m2(x)).flatMap(y => Try(m3(y)))).getOrElse("")

    val recoverRes: Try[Object] = Try(m1(key)).flatMap(x => Try(m2(x)).flatMap(y => Try(m3(y)))).recover {
      case _: NoSuchElementException => Try(m2(key.toInt))
    }

    println(recoverRes.get)

    m1.get(key).flatMap(x => m2.get(x).flatMap(y => m3.get(y))) match {
      case Some(res) => res
      case None => throw new NoSuchElementException
    }
    Try(m1(key)).flatMap(x => Try(m2(x)).flatMap(y => Try(m3(y)))) match {
      case Success(res) => logger.info(key, res)
      case Failure(e) => logger.error(s"$e")
    }
  }

  def futureFlatMap(key: String): Unit = {
    val m1: Map[String, Int] = Map("a" -> 1, "b" -> 2, "e" -> 5)
    val m2: Map[Int, String] = Map(1 -> "west", 2 -> "east", 3 -> "north", 4 -> "south")
    val m3: Map[String, String] = Map("west" -> "left", "east" -> "right", "north" -> "up", "south" -> "down")
    import scala.concurrent.ExecutionContext.Implicits.global
    val futureTask: Future[String] = Future(m1(key)).flatMap(x => Future(m2(x)).flatMap(y => Future(m3(y))))

    futureTask.onComplete {
      case Failure(e) => println(e.getMessage)
    }
    logger.info(key, Await.result(futureTask, Duration.Inf))

    val inc: Int => Int = (a: Int) => a + 1
    val dec: Int => Int = (b: Int) => b - 2
    val pipeline: Int => Int = Function.chain(Seq(inc, dec))
    pipeline.apply(12)
    pipeline(13)
  }


  /**
   * Lambda表达式, 匿名函数
   * */
  def m1: (Int) => Int = (x: Int) => x * x

  /**
   * 偏应用函数
   * */
  def sum(a: Int, b: Int, c: Int): Int = a + b + c

  /**
   * Curry函数
   * */
  def curriedSum(x: Int)(y: Int): Int = x + y
}
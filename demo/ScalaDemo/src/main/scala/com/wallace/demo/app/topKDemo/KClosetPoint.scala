package com.wallace.demo.app.topKDemo

/**
  * Created by 10192057 on 2018/6/22 0022.
  */
object KClosetPoint {

  case class Point(x: Int, y: Int)

  case object Point {
    val zero: Point = Point(0, 0)
  }

  def equal(a: Point, b: Point): Boolean = a.x == b.x && a.y == b.y

  def distance(start: Point, end: Point): Double = {
    Math.sqrt(Math.pow(Math.abs(start.x - end.x), 2) + Math.pow(Math.abs(start.y - end.y), 2))
  }

  def kCloset(dataSet: Array[Point], origin: Point, k: Int): Array[Point] = {
    assert(k <= dataSet.length, s"Array Index Out Of Bounds Exception: K should less than ${dataSet.length}")
    val temp: Array[Point] = dataSet.sortWith {
      case (a, b) =>
        val d_A = distance(a, origin)
        val d_B = distance(b, origin)
        if (d_A == d_B) {
          if (a.x != b.x) a.x < b.x else a.y < b.y
        } else {
          d_A < d_B
        }
    }
    if (k < (dataSet.length >> 1)) {
      temp.take(k)
    } else {
      temp.dropRight(dataSet.length - k)
    }
  }

  def main(args: Array[String]): Unit = {
    val r = new java.util.Random(1000)
    val data: Array[Point] = (0 until 10000).map {
      i =>
        Point(r.nextInt() % 10000 + i, r.nextInt() % 10000)
    }.toArray
    //    val data: Array[Point] = Array(Point(3, 4), Point(3, 2), Point(1, 4), Point(4, 6), Point(5, 3), Point(-1, 4), Point(3, 45), Point(4, 5), Point(30, 4), Point(35, 2), Point(13, 4), Point(34, 56), Point(35, 36), Point(-51, 4), Point(39, 45), Point(-2344, 56)
    //      , Point(13, 14), Point(33, 22), Point(121, 43), Point(434, 6), Point(52, 31), Point(32, 421), Point(93, 45), Point(34, 75), Point(305, 45), Point(5365, 256), Point(1653, 54), Point(343, 556), Point(3655, 3665), Point(-551, 44), Point(4539, 245), Point(-4334, 456)
    //      , Point(43, -54), Point(3, -42), Point(-21, 44), Point(-24, -46), Point(45, -43), Point(-41, 54), Point(43, 4545), Point(41, 89), Point(230, 54), Point(535, 52), Point(153, 46), Point(634, 567), Point(375, 326), Point(-551, 64), Point(369, 4562), Point(-62344, 566),
    //      Point(13, 312), Point(23, -14))
    val startTime = System.nanoTime()
    val res = kCloset(data, Point.zero, 100)
    val endTime = System.nanoTime()
    val duration = (endTime - startTime) / 1000000
    res.foreach(println)

    val expect = data.map(p => (distance(p, Point.zero), p)).sortBy(_._1).map(_._2)
    res.indices.foreach {
      i =>
        assert(equal(expect(i), res(i)), "Failed to get K-Closet Point.")
    }
    println(s"CostTime: $duration ms")
  }
}

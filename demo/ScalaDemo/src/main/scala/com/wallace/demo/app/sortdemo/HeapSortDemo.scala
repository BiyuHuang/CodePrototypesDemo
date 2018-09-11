package com.wallace.demo.app.sortdemo

/**
  * Created by 10192057 on 2018/9/10 0010.
  */
object HeapSortDemo {
  def main(args: Array[String]): Unit = {
    val data = Array(1, 3, 2, 6, 5, 7, 8, 9, 10, 0)
    heapSort[Int](_ > _)(data)
    println("=" * 100)
    data.foreach(println)
  }

  def heapSort[T](comparator: (T, T) => Boolean)(data: Array[T]): Unit = {
    (0 until (data.length - 2) / 2).reverse.foreach {
      i =>
        downAndAdjust(comparator)(data, i, data.length)
    }
    data.foreach(println)

    (1 until (data.length - 1)).reverse.foreach {
      i =>
        val temp = data(i)
        data.update(i, data(0))
        data.update(0, temp)
        downAndAdjust(comparator)(data, 0, i)
    }
  }

  def downAndAdjust[T](comparator: (T, T) => Boolean)(data: Array[T], parentIndex: Int, length: Int): Unit = {
    val temp: T = data(parentIndex)
    var pIndex = parentIndex
    var childIndex: Int = 2 * pIndex + 1
    while (childIndex < length) {
      if (childIndex + 1 < length && comparator(data(childIndex + 1), data(childIndex))) {
        childIndex += 1
      }

      if (comparator(temp, data(childIndex))) {
        return
      }

      data.update(pIndex, data(childIndex))
      pIndex = childIndex
      childIndex = 2 * childIndex + 1
    }
    data.update(pIndex, temp)
  }

}

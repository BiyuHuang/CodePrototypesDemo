package com.wallace.demo.app.sortdemo

/**
  * Created by 10192057 on 2018/9/10 0010.
  * 最大堆排序算法的步骤：
  * 1. 把无序数组构建成二叉堆。
  * 2. 循环删除堆顶元素，移到集合尾部，调节堆产生新的堆顶。
  */
object HeapSortDemo {
  def main(args: Array[String]): Unit = {
    val data = Array(1, 3, 2, 6, 5, 7, 8, 9, 10, 0)
    heapSort[Int](_ > _)(data)
    println("=" * 100)
    data.foreach(println)
  }

  def heapSort[T](comparator: (T, T) => Boolean)(data: Array[T]): Unit = {

    (0 to (data.length - 2) / 2).reverse.foreach {
      i =>
        println("index1: ", i)
        downAndAdjust(comparator)(data, i, data.length)
    }
    data.foreach(println)

    (1 until data.length).reverse.foreach {
      i =>
        println("index2: ", i)
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

package com.wallace.demo.app.sortdemo

import com.wallace.demo.app.common.LogSupport

/**
  * Created by 10192057 on 2018/9/10 0010.
  * 最大堆排序算法的步骤：
  * 1. 把无序数组构建成二叉堆。
  * 2. 循环删除堆顶元素，移到集合尾部，调节堆产生新的堆顶。
  */
object HeapSortDemo extends LogSupport {
  def main(args: Array[String]): Unit = {
    val list = Array[Int](1, 4, 3, 5, 6, 8, 2, 3, 89, 4, 34, 50)
    val sortedList: Array[Int] = generateHeap(list)
    printlnList(sortedList)
    //添加新元素
    printlnList(insertToHeap(sortedList, 76))
    //删除
    printlnList(deleteFromHeap(sortedList, 3))
    //堆排序,按大到小输出元素
    sortAll(sortedList)

    //unchecked
    val data = Array(1, 3, 2, 6, 5, 7, 8, 9, 10, 0)
    heapSort[Int](_ > _)(data)
    println("=" * 100)
    data.foreach(println)
  }

  def printlnList(sortedHeap: Array[Int]): Unit = {
    sortedHeap.foreach(item => print(s"$item "))
    println("sorted!")
    //    for (k <- sortedHeap.indices) {
    //      println(s"($k->${sortedHeap(k)})")
    //    }
  }

  private def sortAll(sortedList: Array[Int]): Unit = {
    var tempSortedList: Array[Int] = sortedList
    while (tempSortedList.length > 0) {
      println(s"${sortedList.head} ")
      tempSortedList = generateHeap(tempSortedList.tail)
      //    printlnList(sortedList)
    }
  }

  /**
    * 生成堆结构
    *
    * @param unSortHeap 任意乱序数组
    * @return 符合堆结构的数组
    */
  def generateHeap(unSortHeap: Array[Int]): Array[Int] = {
    val num = unSortHeap.length
    if (num <= 1) {
      unSortHeap
    } else {
      var tempUnSortHeap = unSortHeap
      for (i <- num / 2 - 1 until num) {
        tempUnSortHeap = sort(tempUnSortHeap, i)
      }
      tempUnSortHeap
    }
  }

  /**
    * 排序特定子树
    *
    * @param heap  未排序堆
    * @param index 当前排序索引值
    * @return 某子树已经排序完
    */
  def sort(heap: Array[Int], index: Int): Array[Int] = {
    //父节点
    var childIndex = index

    var parentIndex = (childIndex - 1) / 2
    var temp = heap(childIndex)
    while (parentIndex >= 0 && childIndex != 0) {
      if (heap(parentIndex) < temp) {
        //swap
        heap(childIndex) = heap(parentIndex)
        heap(parentIndex) = temp
      }
      childIndex = parentIndex
      parentIndex = (childIndex - 1) / 2
      temp = heap(childIndex)
    }
    heap
  }

  /**
    * 从堆中删除数据
    *
    * @param sortedHeap 已排序的队列
    * @param index      索引值
    */
  def deleteFromHeap(sortedHeap: Array[Int], index: Int): Array[Int] = {
    val newArray = sortedHeap.toBuffer
    newArray.remove(index)
    generateHeap(newArray.toArray)
  }


  /**
    * 向堆中插入数据
    *
    * @param sortedHeap 已排序的队列
    * @param newElement 添加到对中的元素
    */
  def insertToHeap(sortedHeap: Array[Int], newElement: Int): Array[Int] = {
    val newList = sortedHeap :+ newElement
    sort(newList, newList.length - 1)
  }

  @unchecked
  def heapSort[T](comparator: (T, T) => Boolean)(data: Array[T]): Unit = {

    (0 to (data.length - 2) / 2).reverse.foreach {
      i =>
        log.info(s"index1: $i")
        downAndAdjust(comparator)(data, i, data.length)
    }
    data.foreach(println)

    (1 until data.length).reverse.foreach {
      i =>
        log.info(s"index2: $i")
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

package com.wallace.demo.app.sortdemo

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
  * Created by 10192057 on 2018/8/20 0020.
  */

class ScalaSortDemo {
  //------------------------产生随机序列------------------------------------------

  //产生一组随机数,内部元素可能相同
  def randomList(n: Int): Seq[Int] = Seq.fill(n)(scala.util.Random.nextInt(n))

  //产生一组随机数,内部元素不相同
  def randomDiffList(n: Int): List[Int] = {
    var resultList: List[Int] = Nil
    while (resultList.length < n) {
      val tempNum = (new scala.util.Random).nextInt(n)
      if (!resultList.contains(tempNum)) {
        resultList = resultList ::: List(tempNum)
      }
    }
    resultList
  }

  //-----------------------------排序算法-----------------------------------------
  //implicit  def ListToArray(List1:List[Int]) = List1 toArray
  //implicit def ArrayToList(Array2:Array[Int]) = Array2 toList

  //TODO 直接插入排序(Straight Select Sorting)
  //scala 中有直接的insert函数，和直接插入排序的思想一样，在此省略改插入法
  //  直接插入排序 核心逻辑：
  //  *
  //  *     在要排序的一组数中，假设前面 (n-1)  [n>=2] 个数已经是排好顺序的，
  //  * 现在要把第n 个数插到前面的有序数中，
  //  * 使得这 n个数也是排好顺序的。如此反复循环，直到全部排好顺序。


  //TODO 堆排序（heap sort）
  //  二叉堆满足二个特性：
  //  1．父结点的键值总是大于或等于（小于或等于）任何一个子节点的键值。
  //  2．每个结点的左子树和右子树都是一个二叉堆（都是最大堆或最小堆）。
  //  当父结点的键值总是大于或等于任何一个子节点的键值时为最大堆。当父结点的键值总是小于或等于任何一个子节点的键值时为最小堆
  //  由于其它几种堆（二项式堆，斐波纳契堆等）用的较少，一般将二叉堆就简称为堆。
  // 一般都用数组来表示堆，i结点的父结点下标就为(i – 1) / 2。
  // 它的左右子结点下标分别为2 * i + 1和2 * i + 2。如第0个结点左右子结点下标分别为1和2。
  def buildHeap[T](comparator: (T, T) => Boolean)(source: ListBuffer[T], parent: Int): Unit = {
    if (left(parent) >= source.length) {

      return
    } else {
      buildHeap(comparator)(source, left(parent))
    }
    if (right(parent) >= source.length) {
      return
    } else {
      buildHeap(comparator)(source, right(parent))
    }
    if (comparator(source(left(parent)), source(parent))
      && comparator(source(right(parent)), source(parent))) {
      if (comparator(source(left(parent)), source(right(parent)))) {
        val p = source(parent)
        source(parent) = source(left(parent))
        source(left(parent)) = p
        buildHeap(comparator)(source, left(parent))
      } else {
        val p = source(parent)
        source(parent) = source(right(parent))
        source(right(parent)) = p
        buildHeap(comparator)(source, right(parent))
      }
    } else if (comparator(source(left(parent)), source(parent))) {
      val p = source(parent)
      source(parent) = source(left(parent))
      source(left(parent)) = p
      buildHeap(comparator)(source, left(parent))
    } else if (comparator(source(right(parent)), source(parent))) {
      val p = source(parent)
      source(parent) = source(right(parent))
      source(right(parent)) = p
      buildHeap(comparator)(source, right(parent))
    }
  }

  def left(parent: Int): Int = parent * 2 + 1

  def right(parent: Int): Int = parent * 2 + 2

  def heapFye[T](comparator: (T, T) => Boolean)(source: ListBuffer[T], parent: Int, rightIndex: Int): Unit = {
    if (left(parent) > rightIndex) {
      return
    }
    /*if(right(parent)>rightIndex){
      return
    }*/
    if (left(parent) <= rightIndex && right(parent) > rightIndex) {
      if (comparator(source(left(parent)), source(parent))) {
        val p = source(parent)
        source(parent) = source(left(parent))
        source(left(parent)) = p
        heapFye(comparator)(source, left(parent), rightIndex)
      }
    } else {
      if (comparator(source(left(parent)), source(parent))
        && comparator(source(right(parent)), source(parent))) {
        if (comparator(source(left(parent)), source(right(parent)))) {
          val p = source(parent)
          source(parent) = source(left(parent))
          source(left(parent)) = p
          heapFye(comparator)(source, left(parent), rightIndex)
        } else {
          val p = source(parent)
          source(parent) = source(right(parent))
          source(right(parent)) = p
          heapFye(comparator)(source, right(parent), rightIndex)
        }
      } else if (comparator(source(left(parent)), source(parent))) {
        val p = source(parent)
        source(parent) = source(left(parent))
        source(left(parent)) = p
        heapFye(comparator)(source, left(parent), rightIndex)
      } else if (comparator(source(right(parent)), source(parent))) {
        val p = source(parent)
        source(parent) = source(right(parent))
        source(right(parent)) = p
        heapFye(comparator)(source, right(parent), rightIndex)
      }
    }
  }

  def heapSort[T](comparator: (T, T) => Boolean)(source: ListBuffer[T], rightIndex: Int): ListBuffer[T] = {
    for (i <- (1 until source.length).reverse) {
      val tmp = source(i)
      source(i) = source.head
      source(0) = tmp
      heapFye(comparator)(source, 0, i - 1)
    }
    source
  }

  //TODO 计数排序
  def countSort(inputData: ArrayBuffer[Int], k: Int): Array[Int] = {
    //k表示有所输入数字都介于0到k之间
    val temp = new Array[Int](k)
    // 临时存储区
    val outdata = new Array[Int](inputData.length)
    val len = temp.length
    for (i <- 0 until len) {
      // 初始化
      temp(i) = 0
    }
    for (i <- inputData.indices) {
      temp(inputData(i)) = temp(inputData(i)) + 1
    }
    for (i <- 1 until len) {
      temp(i) = temp(i) + temp(i - 1)
    }
    // 把输入数组中的元素放在输出数组中对应的位置上
    var n = inputData.length - 1
    while (n >= 0) {
      // 从后往前遍历
      outdata(temp(inputData(n)) - 1) = inputData(n)
      temp(inputData(n)) = temp(inputData(n)) - 1
      n = n - 1
    }
    outdata
  }

  //TODO 桶排序
  def bucketSort(inputData: ArrayBuffer[Int], max: Int): ArrayBuffer[Int] = {
    var buckets = new Array[Int](max)
    for (i <- inputData.indices) //计数
      buckets(inputData(i)) = buckets(inputData(i)) + 1
    var j = 0
    for (i <- 0 until max)
      while (buckets(i) > 0) {
        inputData(j) = i
        j = j + 1
        buckets(i) = buckets(i) - 1
      }
    buckets = null
    inputData
  }

  //TODO 基数排序函数
  /** B表示要排序的数组
    * d表示每一位数字的范围（这里是10进制数，有0~9一共10种情况）
    */
  def radixSort(inputData: ArrayBuffer[Int], d: Int): ArrayBuffer[Int] = {
    import scala.util.control.Breaks._
    //n用来表示当前排序的是第几位
    var n = 1
    //hasNum用来表示数组中是否有至少一个数字存在第n位
    var hasNum = false
    /** 二维数组temp用来保存当前排序的数字
      * 第一维d表示一共有d个桶
      * 第二维B.length表示每个桶最多可能存放B.length个数字
      */
    val temp = Array.ofDim[Int](d, inputData.length)
    val order = new Array[Int](d)
    breakable {
      while (true) {
        //判断是否所有元素均无比更高位，因为第一遍一定要先排序一次，所以有n!=1的判断
        if (n != 1 && !hasNum) {
          break
        }
        hasNum = false
        //遍历要排序的数组，将其存入temp数组中（按照第n位上的数字将数字放入桶中）
        for (i <- inputData.indices) {
          val x = inputData(i) / (n * 10)
          if (x != 0) hasNum = true
          val lsd = x % 10
          temp(lsd)(order(lsd)) = inputData(i)
          order(lsd) = order(lsd) + 1
        }
        //k用来将排序好的temp数组存入B数组（将桶中的数字倒出）
        var k = 0
        for (i <- 0 until d) {
          if (order(i) != 0) {
            var j = 0
            while (j < order(i)) {
              inputData(k) = temp(i)(j)
              k = k + 1
              j = j + 1
            }
          }
          order(i) = 0
        }
        n = n + 1
      }
    }
    inputData
  }


  //TODO 希尔排序（shell sort）
  def shellSort(SortList: List[Int]): List[Int] = {
    val cpSortList = SortList.toArray
    var d = cpSortList.length
    while (d > 1) {
      d = math.floor(d / 2).toInt
      for (i <- 0 to d) {
        //小组内排好序
        for (j <- Range(i, cpSortList.length, d)) {
          var minIndex = j
          for (o <- Range(j + d, cpSortList.length, d)) {
            if (cpSortList(minIndex) > cpSortList(o)) { //cpSortList(minIndex) < cpSortList(j) 从大到小
              minIndex = o
            }
          }
          val temp = cpSortList(j)
          cpSortList(j) = cpSortList(minIndex)
          cpSortList(minIndex) = temp
        }
      }
    }
    cpSortList.toList
  }

  //简单选择排序(Selection sort)
  def selectSort(SortList: List[Int]): List[Int] = {
    val cpSortList = SortList.toArray
    for (i <- cpSortList.indices) {
      var minIndex = i
      //找到当前循环的最小值
      for (j <- i + 1 until cpSortList.length) {
        if (cpSortList(minIndex) > cpSortList(j)) {
          //cpSortList(minIndex) < cpSortList(j) 从大到小
          minIndex = j
        }
      }
      val temp = cpSortList(i)
      cpSortList(i) = cpSortList(minIndex)
      cpSortList(minIndex) = temp
    }
    cpSortList.toList
  }

  //冒泡排序（Bubble Sort）
  def bubbleSort(SortList: List[Int]): List[Int] = {
    val cpSortList = SortList.toArray
    for (i <- 0 until cpSortList.length - 1; j <- 0 until cpSortList.length - 1 - i) {
      if (cpSortList(j) > cpSortList(j + 1)) {
        val tmp = cpSortList(j)
        cpSortList(j) = cpSortList(j + 1)
        cpSortList(j + 1) = tmp
      }
    }
    cpSortList.toList
  }

  //快速排序（quickSort）
  //《Scala By Example》的一个列子
  def quickSort(SortList: Array[Int]): Array[Int] = {
    if (SortList.length <= 1) {
      SortList
    } else {
      val pivot = SortList(SortList.length / 2)
      Array.concat(
        quickSort(SortList.filter(x => pivot > x)),
        SortList.filter(y => pivot == y),
        quickSort(SortList.filter(z => pivot < z))
      )
    }
  }


  //归并排序
  def mergeSort(SortList: List[Int]): List[Int] = {
    def merge(a: List[Int], b: List[Int]): List[Int] = (a, b) match {
      case (Nil, _) => b
      case (_, Nil) => a
      case (x :: xs, y :: ys) =>
        if (x <= y) x :: merge(xs, b) else y :: merge(a, ys)
    }

    if (SortList.length == 1) {
      SortList
    } else {
      val (first, second) = SortList.splitAt(SortList.length / 2)
      merge(mergeSort(first), mergeSort(second))
    }
  }
}

object SortDemo {
  def main(args: Array[String]): Unit = {
    val sortDemo = new ScalaSortDemo()
    val rdmList: List[Int] = sortDemo.randomDiffList(100)
    val source: ListBuffer[Int] = ListBuffer().++=:(rdmList)
    //val rdmNum = scala.util.Random.nextInt(10)
    println("排序前：" + rdmList)
    println("简单选择排序：" + sortDemo.selectSort(rdmList))
    println("希尔排序：" + sortDemo.shellSort(rdmList))
    println("冒泡排序：" + sortDemo.bubbleSort(rdmList))
    println("快速排序：" + sortDemo.quickSort(rdmList.toArray).toList)
    println("归并排序:" + sortDemo.mergeSort(rdmList))

    sortDemo.buildHeap[Int](_ > _)(source, 0)
    println("堆内数据：" + source.toList)
    println("堆排序: " + sortDemo.heapSort[Int](_ > _)(source, source.length - 1))
    //排序前：List(3, 9, 8, 7, 0, 1, 4, 5, 6, 2)
    //简单选择排序：List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    //希尔排序：List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    //冒泡排序：List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    //快速排序：List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    //归并排序:List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
  }
}

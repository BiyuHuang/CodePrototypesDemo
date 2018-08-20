package com.wallace.demo.app.sortdemo

/**
  * Created by 10192057 on 2018/8/20 0020.
  */
class ParHeapSort {
  val combop: (List[Int], List[Int]) => List[Int] = (ls1: List[Int], ls2: List[Int]) => ls1.:::(ls2)

  val seqop: (List[Int], Int) => List[Int] = (ls: List[Int], value: Int) => ls.::(value)

  Array(7, 11, 9, 17, 15, 21, 8, 30, 14, 0, 12, 15, 55, 2, 3, 18, 22, 23, 4).aggregate(List[Int]())(seqop, combop)

}

import scala.collection.mutable

/**
  * 实现并行堆排序算法
  *
  */
class HeapSort[A, B, S <: Iterable[A]](f: A => B)(implicit ord: Ordering[B]) {
  val combop: (List[Int], List[Int]) => List[Int] = (ls1: List[Int], ls2: List[Int]) => ls1.:::(ls2)

  val seqop: (List[Int], Int) => List[Int] = (ls: List[Int], value: Int) => ls.::(value)

  /**
    * 对l排序返回排序后的Seq
    *
    * @param l    待排序集合的迭代器
    * @param desc 降/升序(默认为true,降序)
    * @return
    */
  def sort(l: S, desc: Boolean = true): mutable.Seq[A] = HeapSort.sort(f)(l, 0, desc)

  /**
    * 对l排序并返回前top个结果
    *
    * @param l    待排序集合的迭代器
    * @param top  返回最多结果数目
    * @param desc 降/升序(默认为true,降序)
    * @return
    */
  def top(l: S, top: Int, desc: Boolean = true): mutable.Seq[A] = HeapSort.sort(f)(l, top, desc)

  /**
    * 对可变集合排序，返回排序后的Seq
    *
    * @param l    待排序可变集合的迭代器
    * @param desc 降/升序(默认为true,降序)
    * @return
    */
  def sortM[M <: mutable.Seq[A]](l: M, desc: Boolean = true): mutable.Seq[A] = HeapSort.sortMutable(f)(l, 0, desc)

  /**
    * 对可变集合l排序并返回前top个结果
    *
    * @param l    待排序可变集合的迭代器
    * @param top  返回最多结果数目
    * @param desc 降/升序(默认为true,降序)
    * @return
    */
  def topM[M <: mutable.Seq[A]](l: M, top: Int, desc: Boolean = true): mutable.Seq[A] = HeapSort.sortMutable(f)(l, top, desc)

  /**
    * 对可变集合l并行排序并返回前top个结果
    *
    * @param l    待排序可变集合的迭代器
    * @param top  返回最多结果数目
    * @param desc 降/升序(默认为true,降序)
    * @return
    */
  def topParM[M <: mutable.Seq[A]](l: M, top: Int, desc: Boolean = true): mutable.Seq[A] = HeapSort.topParMutable(f)(l, top, desc)

  /**
    * 对可变集合l的指定范围排序并返回排序后的Seq
    *
    * @param seq   待排序可变集合
    * @param top   返回最多结果数目
    * @param desc  降/升序(默认为true,降序)
    * @param from  待排序的起始位置
    * @param until 待排序的结束位置
    * @return
    */
  def sortRange[M <: mutable.Seq[A]](seq: M, top: Int, desc: Boolean = true)(from: Int = 0, until: Int = seq.length): (Int, Int) = {
    HeapSort.sortMutableRange(f)(seq, top, desc)(from, until)
  }

  /**
    * 对seq中两个已经排序的区段进行合并排序，将src合并到dst
    *
    * @param seq  可变集合
    * @param src  待合并的源区段(起始位置，结束位置)
    * @param dst  待合并的目标区段(起始位置，结束位置)
    * @param desc 降/升序(默认为true,降序)
    * @return
    */
  def merge2Seq(seq: mutable.Seq[A], src: (Int, Int), dst: (Int, Int), desc: Boolean = true): (Int, Int) = HeapSort.merge2Seq(f)(seq, src, dst, desc)

  /**
    * 对seq中两个已经排序的区段进行合并排序，将src合并到dst
    *
    * @param seq  可变集合
    * @param src  待合并的源区段(起始位置，结束位置)
    * @param dst  待合并的目标区段(起始位置，结束位置)
    * @param desc 降/升序(默认为true,降序)
    * @return
    */
  def merge2Seq2(seq: mutable.Seq[A], src: (Int, Int), dst: (Int, Int), desc: Boolean = true): (Int, Int) = HeapSort.merge2Seq2(f)(seq, src, dst, desc)

  /**
    * 对seq中两个已经排序的区段进行合并排序，将src合并到dst<br>
    * 该算法在排序过程不申请新内存
    *
    * @param seq  可变集合
    * @param src  待合并的源区段(起始位置，结束位置)
    * @param dst  待合并的目标区段(起始位置，结束位置)
    * @param desc 降/升序(默认为true,降序)
    * @return
    */
  def merge2SeqNM(seq: mutable.Seq[A], src: (Int, Int), dst: (Int, Int), desc: Boolean = true): (Int, Int) = HeapSort.merge2SeqNM(f)(seq, src, dst, desc)
}

object HeapSort {
  def sort[A, B, S <: Iterable[A]](f: A => B)(iterator: S, top: Int = 0, desc: Boolean = true)(implicit ord: Ordering[B]): mutable.Seq[A] = {
    val bf = iterator.toBuffer
    sortMutable(f)(bf, top, desc)
  }

  def sortMutable[A, B, S <: mutable.Seq[A]](f: A => B)(seq: S, top: Int = 0, desc: Boolean = true)(implicit ord: Ordering[B]): mutable.Seq[A] = {
    sortMutableRange(f)(seq, top, desc)()
    (if (top < seq.length && top > 0) seq.takeRight(top) else seq).reverse
  }

  private def sortMutableRange[A, B, S <: mutable.Seq[A]](f: A => B)(seq: S, top: Int = 0, desc: Boolean = true)(from: Int = 0, until: Int = seq.length)(implicit ord: Ordering[B]) = {
    buildHeapRange(f)(seq, desc)(from, until); // 构建堆

    val sublen = until - from
    val toplen = if (top <= 0 || top >= sublen) sublen else top
    var i = until - 1
    var continue = true
    while (continue) {
      swap(seq, from, i)
      if (i > (until - toplen)) {
        heapFye(f)(seq, from, i, desc, from)
        i -= 1
      } else continue = false
    }
    (i, until)
  }

  private def buildHeapRange[A, B](f: A => B)(seq: mutable.Seq[A], desc: Boolean)(from: Int, until: Int)(implicit ord: Ordering[B]): Unit = {
    var i = from + ((until - from) >>> 1) - 1
    while (i >= from) {
      heapFye(f)(seq, i, until, desc, from)
      i -= 1
    }
  }

  def cmp1Gt[A, B](f: A => B)(l: A, r: A)(implicit ord: Ordering[B]): Boolean = ord.gt(f(l), f(r))

  def cmp1Lt[A, B](f: A => B)(l: A, r: A)(implicit ord: Ordering[B]): Boolean = ord.lt(f(l), f(r))

  def cmpGt[A, B](f: A => B, seq: mutable.Seq[A])(l: Int, r: Int)(implicit ord: Ordering[B]): Boolean = cmp1Gt(f)(seq(l), seq(r))

  def cmpLt[A, B](f: A => B, seq: mutable.Seq[A])(l: Int, r: Int)(implicit ord: Ordering[B]): Boolean = cmp1Lt(f)(seq(l), seq(r))

  private def heapFye[A, B](f: A => B)(seq: mutable.Seq[A], startpos: Int, max: Int, desc: Boolean, off: Int)(implicit ord: Ordering[B]): Unit = {
    def gt = (l: Int, r: Int) => cmpGt(f, seq)(l, r)

    def lt = (l: Int, r: Int) => cmpLt(f, seq)(l, r)

    val cmp = if (desc) gt else lt
    var largest = 0
    var idx = startpos
    var right = 0
    var left = 0
    do {
      right = off + ((idx - off + 1) << 1)
      left = right - 1
      largest = if (left < max && cmp(left, idx)) left else idx
      if (right < max && cmp(right, largest)) {
        largest = right
      }
      if (largest != idx) {
        swap(seq, largest, idx)
        idx = largest
      } else {
        return
      }
    } while (true)
  }

  private def swap[A](seq: mutable.Seq[A], i: Int, j: Int): Unit = {
    val temp = seq(i)
    seq(i) = seq(j)
    seq(j) = temp
  }

  private def swap3[A](seq: mutable.Seq[A], i: Int, j: Int, k: Int): Unit = {
    val temp = seq(i)
    seq(i) = seq(j)
    seq(j) = seq(k)
    seq(k) = temp
  }

  //  private def _duplicateSeq[A](src: collection.Seq[A], srcPos: Int, dest: mutable.Seq[A], destPos: Int, length: Int): mutable.Seq[A] = {
  //    for (i <- 0 until length) dest(destPos + i) = src(srcPos + i)
  //    dest
  //  }

  private def duplicateSeq[A](src: collection.Seq[A], srcPos: Int, dest: mutable.Seq[A], destPos: Int, length: Int): mutable.Seq[A] = {
    var i = 0
    while (i < length) {
      dest(destPos + i) = src(srcPos + i)
      i += 1
    }
    dest
  }

  def merge2Seq[A, B](f: A => B)(seq: mutable.Seq[A], src: (Int, Int), dst: (Int, Int), desc: Boolean)(implicit ord: Ordering[B]): (Int, Int) = {
    if (!(if (desc) cmpGt(f, seq)(dst._1, src._2 - 1) else cmpLt(f, seq)(dst._1, src._2 - 1))) {
      if (if (desc) cmpGt(f, seq)(src._1, dst._2 - 1) else cmpLt(f, seq)(src._1, dst._2 - 1)) {
        val (srclen, dstlen) = (src._2 - src._1, dst._2 - dst._1)
        val cplen = math.min(srclen, dstlen)
        duplicateSeq(seq, dst._1 + cplen, seq, dst._1, dstlen - cplen)
        duplicateSeq(seq, src._2 - cplen, seq, dst._2 - cplen, cplen)
      } else {
        val q = mutable.Queue[A]()

        def gt = (r: Int) => cmp1Gt(f)(seq(r), q.head)

        def lt = (r: Int) => cmp1Lt(f)(seq(r), q.head)

        val cmpdst: Int => Boolean = if (desc) gt else lt
        var (topsrc, idx) = (src._2 - 1, dst._2 - 1)
        while (idx >= dst._1) {
          q.enqueue(seq(idx))
          if (cmpdst(topsrc)) {
            seq(idx) = seq(topsrc)
            topsrc -= 1
          } else {
            seq(idx) = q.dequeue()
          }
          idx -= 1
        }
        while (idx >= dst._1) {
          seq(idx) = q.dequeue()
          idx -= 1
        }
      }
    }
    dst
  }

  def merge2Seq2[A, B](f: A => B)(seq: mutable.Seq[A], src: (Int, Int), dst: (Int, Int), desc: Boolean)(implicit ord: Ordering[B]): (Int, Int) = {
    if (!(if (desc) cmpGt(f, seq)(dst._1, src._2 - 1) else cmpLt(f, seq)(dst._1, src._2 - 1))) {
      if (if (desc) cmpGt(f, seq)(src._1, dst._2 - 1) else cmpLt(f, seq)(src._1, dst._2 - 1)) {
        val (srclen, dstlen) = (src._2 - src._1, dst._2 - dst._1)
        val cplen = math.min(srclen, dstlen)
        duplicateSeq(seq, dst._1 + cplen, seq, dst._1, dstlen - cplen)
        duplicateSeq(seq, src._2 - cplen, seq, dst._2 - cplen, cplen)
      } else {
        val q = seq.slice(dst._1, dst._2)

        def gt = (l: Int, r: Int) => cmp1Gt(f)(seq(l), q(r))

        def lt = (l: Int, r: Int) => cmp1Lt(f)(seq(l), q(r))

        val cmpdst = if (desc) gt else lt
        var (topdst, topsrc, idx) = (q.length - 1, src._2 - 1, dst._2 - 1)
        while (idx >= dst._1 && topsrc >= src._1) {
          if (cmpdst(topsrc, topdst)) {
            seq(idx) = seq(topsrc)
            topsrc -= 1
          } else {
            seq(idx) = q(topdst)
            topdst -= 1
          }
          idx -= 1
        }
        if (idx >= dst._1) {
          duplicateSeq(q, topdst - (idx - dst._1), seq, dst._1, idx - dst._1 + 1)
        }
      }
    }
    dst
  }

  def merge2SeqNM[A, B](f: A => B)(seq: mutable.Seq[A], src: (Int, Int), dst: (Int, Int), desc: Boolean)(implicit ord: Ordering[B]): (Int, Int) = {
    if (!(if (desc) cmpGt(f, seq)(dst._1, src._2 - 1) else cmpLt(f, seq)(dst._1, src._2 - 1))) {
      if (if (desc) cmpGt(f, seq)(src._1, dst._2 - 1) else cmpLt(f, seq)(src._1, dst._2 - 1)) {
        val (srclen, dstlen) = (src._2 - src._1, dst._2 - dst._1)
        val cplen = math.min(srclen, dstlen)
        duplicateSeq(seq, dst._1 + cplen, seq, dst._1, dstlen - cplen)
        duplicateSeq(seq, src._2 - cplen, seq, dst._2 - cplen, cplen)
      } else {
        var (idx, qbf, qbt, qh) = (dst._2 - 1, dst._2 - 1, dst._2 - 1, dst._2 - 1)
        var st = src._2 - 1
        var swapst = () => {}
        var swapqh = () => {}

        def gt = (l: Int) => cmpGt(f, seq)(l, qh)

        def lt = (l: Int) => cmpLt(f, seq)(l, qh)

        val cmpdst = if (desc) gt else lt

        def swapTop(top: Int): Unit = {
          val temp = seq(idx)
          seq(idx) = seq(top)
          seq(top) = temp
        }

        def getQl = () => qbf + (qh - qbf + 1) % (qbt - qbf + 1)

        def nextQh = () => qbt - (qbt - qh + 1) % (qbt - qbf + 1)

        //      def moveStep(from: Int, to: Int, step: Int) =for (i <- (if (step > 0) (from to to).reverse else (from to to))) seq(i + step) = seq(i)
        def moveStep(from: Int, to: Int, step: Int): Unit = {
          var i = if (step > 0) to else from

          def upf() = i >= from

          def dnt() = i <= to

          val (s, c) = if (step > 0) (-1, upf _) else (1, dnt _)
          while (c()) {
            seq(i + step) = seq(i)
            i += s
          }
        }

        def swapLeft(from: Int, to: Int): Unit = {
          val tmp = seq(from - 1)
          moveStep(from, to, -1)
          seq(to) = tmp
        }

        def swapRight(from: Int, to: Int): Unit = {
          val tmp = seq(to + 1)
          moveStep(from, to, 1)
          seq(from) = tmp
        }

        def swapStTail(): Unit = {
          swapTop(st)
          val ql = getQl()
          if (ql > qbf)
            if (qh - qbf > qbt - ql) {
              swap(seq, st, qbt)
              swapRight(ql, qbt - 1)
              qbf = st
            } else {
              swapLeft(qbf, qh)
              qbf = st
              qh = nextQh()
            }
          else {
            qbf = st
          }
        }

        def swapStHead(): Unit = {
          swapTop(st)
          swapst = swapStTail
          swapqh = swapQhEnable
          qh = st
          qbf = st
          qbt = st
        }

        def swapQhDisable(): Unit = {
          qbf -= 1
          qbt -= 1
          qh -= 1
        }

        def swapQhEnable(): Unit = {
          swapTop(qh)
          qh = nextQh()
        }

        swapst = swapStHead
        swapqh = swapQhDisable
        while (idx >= dst._1 && st >= src._1) {
          if (cmpdst(st)) {
            swapst()
            st -= 1
          } else {
            swapqh()
          }
          idx -= 1
        }
        if (idx >= dst._1) {
          val ql = getQl()
          duplicateSeq(seq, ql, seq, dst._1, qbt - ql + 1)
          duplicateSeq(seq, qbf, seq, dst._1 + qbt - ql + 1, ql - qbf)
        }
      }
    }
    dst
  }

  private val processors = Runtime.getRuntime.availableProcessors()

  //获取cpu核心数
  def topParMutable[A, B, M <: mutable.Seq[A]](f: A => B)(seq: M, top: Int, desc: Boolean = true)(implicit ord: Ordering[B]): mutable.Seq[A] = {
    //根据cpu核心数对要排序的数据分段
    val step = (seq.length + processors - 1) / processors
    //以并行方式对每一段数据进行排序
    val rangs = for (i <- (0 until (seq.length + step - 1) / step).par) yield {
      sortMutableRange(f)(seq, top)(i * step, math.min(seq.length, (i + 1) * step))
    }

    def merge: ((Int, Int), (Int, Int)) => (Int, Int) = (left: (Int, Int), right: (Int, Int)) =>
      if ((right._2 - right._1) > (left._2 - left._1)) {
        merge2SeqNM(f)(seq, left, right, desc)
      } else {
        merge2SeqNM(f)(seq, right, left, desc)
      }

    //调用用reduce对分段排序后的结果进行合并
    val r = rangs.reduce(merge)
    //返回排序结果(需要反序)
    seq.slice(r._1, r._2).reverse
  }

  def main(args: Array[String]): Unit = {
    //测试代码
    val m = new HeapSort[Int, Int, mutable.Buffer[Int]]((w: Int) => w)
    println(Array(7, 11, 9, 17, 15, 21, 8, 30, 14, 0, 12, 15, 55, 2, 3, 18, 22, 23, 4).aggregate(List[Int]())(m.seqop, m.combop).toString())
    val rnd = new java.util.Random()
    val l = Array.tabulate[Int](40)((x: Int) => rnd.nextInt(x + 100))
    for (i <- 0 to 0) {
      println("==============time ", i, "=================")
      val s = l.toBuffer[Int]
      println("=========> s: " + s)
      val t1: Long = System.currentTimeMillis
      val r1: (Int, Int) = m.sortRange(s, 10)(0, 20)
      val r2: (Int, Int) = m.sortRange(s, 10)(20, 40)
      val t2: Long = System.currentTimeMillis
      printf("sort time cost:%f seconds(%d mills) used\n", (t2 - t1) / 1024D, t2 - t1)
      for (i <- r1._1 until r1._2) {
        print(s(i) + ",")
      }
      println("=========> r1: " + r1)

      for (i <- r2._1 until r2._2) {
        print(s(i) + ",")
      }
      println("=========> r2: " + r2)

      m.merge2Seq2(s, r1, r2)
      for (i <- (r2._1 until r2._2).reverse) {
        print(s(i) + ",")
      }
      println("=========> Res: " + r2)
    }
  }
}
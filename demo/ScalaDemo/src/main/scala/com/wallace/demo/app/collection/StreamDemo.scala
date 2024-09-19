/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.collection

import com.wallace.demo.app.common.Using

/**
  * Created by wallace on 2019/1/31.
  */
object StreamDemo extends Using {
  def main(args: Array[String]): Unit = {
    //Stream 无限序列，具有延迟计算特性
    val fibs: Stream[Int] = {
      def op(a: Int, b: Int): Stream[Int] = a #:: op(b, a + b)

      op(0, 1)
    }

    fibs.take(10).foreach(logRecord(_))

    fibs.take(20).foreach(logRecord(_))
  }
}

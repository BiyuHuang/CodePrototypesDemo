/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.spark.graphxdemo

import com.wallace.common.Using
import com.wallace.spark.sparkdemo.dataframedemo.WindowExprDemo.createSparkSession
import org.apache.spark.graphx.{Graph, GraphLoader}

/**
  * Created by wallace on 2019/1/9.
  */
object GraphXDemo extends Using {
  def main(args: Array[String]): Unit = {
    tryCatchFinally(createSparkSession("GraphXDemo"), "Failed to run GraphXDemo") {
      spark =>
        val graph: Graph[Int, Int] = GraphLoader.edgeListFile(spark.sparkContext, "")
        graph.pageRank(0.001)
    }
  }
}
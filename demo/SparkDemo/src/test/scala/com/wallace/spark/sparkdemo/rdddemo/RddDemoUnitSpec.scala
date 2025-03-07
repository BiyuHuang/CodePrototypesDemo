/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.spark.sparkdemo.rdddemo

import java.io.File

import com.wallace.UnitSpec

/**
  * com.wallace.spark.sparkdemo.rdddemo
  * Created by 10192057 on 2017/12/19 0019.
  */
class RddDemoUnitSpec extends UnitSpec {
  runTest("unit test for readTextFile") {

    val srcFile: String = if (new File(s"${System.getProperty("user.dir")}/src/test/resources/trainingData.csv.gz").exists()) {
      s"${System.getProperty("user.dir")}/src/test/resources/trainingData.csv.gz"
    } else {
      s"${System.getProperty("user.dir")}/demo/SparkDemo/src/test/resources/trainingData.csv.gz"
    }

    RddDemo.readTextFile(srcFile)
  }
}

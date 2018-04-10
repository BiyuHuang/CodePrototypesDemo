package com.wallace.spark.sparkdemo.rdddemo

import com.wallace.common.UnitSpecTrait

/**
  * com.wallace.spark.sparkdemo.rdddemo
  * Created by 10192057 on 2017/12/19 0019.
  */
class RddDemoUnitSpec extends UnitSpecTrait {
  runTest("unit test for readTextFile") {
    RddDemo.readTextFile(filePath = RddDemo.path)
  }
}

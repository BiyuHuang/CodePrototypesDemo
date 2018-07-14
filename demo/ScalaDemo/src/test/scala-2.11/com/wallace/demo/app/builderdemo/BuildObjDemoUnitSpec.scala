package com.wallace.demo.app.builderdemo

import com.wallace.demo.app.UnitSpec

/**
  * Created by 10192057 on 2018/6/4 0004.
  */
class BuildObjDemoUnitSpec extends UnitSpec {
  teamID should "do unit test for Builder" in {
    BuildObjDemo.builder()
  }
}

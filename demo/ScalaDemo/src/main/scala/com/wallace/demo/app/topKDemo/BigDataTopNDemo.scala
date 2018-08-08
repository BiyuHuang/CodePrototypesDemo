package com.wallace.demo.app.topKDemo

import com.wallace.demo.app.common.LogSupport

/**
  * Created by 10192057 on 2018/8/8 0008.
  */
object BigDataTopNDemo extends LogSupport {

  def main(args: Array[String]): Unit = {
    //TODO BigData( A: key.hashCode % 10240) => SmallData( a1, a2)
    //TODO (0 until 10240).foreach( Traversal <a1, a2>, Return intersection<a1, a2>)
  }
}

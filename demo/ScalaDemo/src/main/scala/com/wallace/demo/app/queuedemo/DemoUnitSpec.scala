package com.wallace.demo.app.queuedemo

/**
  * Created by 10192057 on 2018/6/4 0004.
  */
object DemoUnitSpec {
  def main(args: Array[String]): Unit = {
    val queueV1 = new DemoQueueV1
    queueV1.put(1)
    queueV1.put(2)
    queueV1.put(3)
    println("=========== " + queueV1.getClass.getSimpleName + " ===========")
    println("Queue Size: " + queueV1.size)
    println("Queue Element: " + queueV1.get())
    println("Queue Size: " + queueV1.size)
    println("Queue Element: " + queueV1.get())
    println("Queue Size: " + queueV1.size)
    println("Queue Element: " + queueV1.get())
    println("Queue Size: " + queueV1.size)

    val queueV2: DemoQueueV2 = new DemoQueueV2
    queueV2.put(-10)
    queueV2.put(20)
    queueV2.put(30)
    println("=========== " + queueV2.getClass.getSimpleName + " ===========")
    println("Queue Size: " + queueV2.size)
    println("Queue Element: " + queueV2.get())
    println("Queue Size: " + queueV2.size)
    println("Queue Element: " + queueV2.get())
    println("Queue Size: " + queueV2.size)


    val queueV3: DemoQueueV3 = new DemoQueueV3
    queueV3.put(-15)
    queueV3.put(25)
    queueV3.put(35)
    println("=========== " + queueV3.getClass.getSimpleName + " ===========")
    println("Queue Size: " + queueV3.size)
    println("Queue Element: " + queueV3.get())
    println("Queue Size: " + queueV3.size)
    println("Queue Element: " + queueV3.get())
    println("Queue Size: " + queueV3.size)
    println("Queue Element: " + queueV3.get())
    println("Queue Size: " + queueV3.size)
  }
}

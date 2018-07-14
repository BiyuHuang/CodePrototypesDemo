package com.wallace.demo.app.queuedemo

/**
  * Created by 10192057 on 2018/6/4 0004.
  */
class DemoQueueV1 extends BasicIntQueue with IntDoubling {
  //TODO 装饰模式 Double
}

class DemoQueueV2 extends BasicIntQueue with IntFiltering {
  //TODO 装饰模式 Filter
}

class DemoQueueV3 extends BasicIntQueue with IntIncrementing {
  //TODO 装饰模式 Increment
}


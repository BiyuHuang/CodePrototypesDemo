package com.wallace.demo.app.patterns.observedemo.newsdemo

import com.wallace.demo.app.UnitSpec

/**
 * Author: biyu.huang
 * Date: 2024/4/8 17:39
 * Description:
 */
class NewsPublisherUnitSpec extends UnitSpec {
  teamID should "execute observer mode" in {
    val publisher: NewsPublisher = new NewsPublisher()
    val observer1: NewsSubscriber = new NewsSubscriber("john")
    val observer2: NewsSubscriber = new NewsSubscriber("jerry")

    publisher.addObservers(observer1, observer2)
    publisher.publishNews("""Breaking News: Florida Doesn’t Feed Idle People""")
  }
}

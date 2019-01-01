/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.redisclient

import com.wallace.demo.app.common.Using

/**
  * Created by wallace on 2018/12/27.
  */
object RedisClientDemo extends Using {
  def main(args: Array[String]): Unit = {
    using(new Jedis("localhost", 6379)) {
      redisCli =>
        log.info(redisCli.ping())
        redisCli.lpush("site-list", "Runoob")
        redisCli.lpush("site-list", "Google")
        redisCli.lpush("site-list", "Taobao")
        redisCli.keys("*").asScala.foreach(println)
    }

  }
}

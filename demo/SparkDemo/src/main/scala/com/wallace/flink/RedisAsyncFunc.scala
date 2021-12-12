package com.wallace.flink

import com.wallace.redis.KVData
import org.apache.flink.streaming.api.functions.async.{ResultFuture, RichAsyncFunction}

/**
  * Created by wallace on 2020/4/25.
  */
class RedisAsyncFunc extends RichAsyncFunction[KVData, KVData]{
  override def asyncInvoke(in: KVData, resultFuture: ResultFuture[KVData]) = ???
}

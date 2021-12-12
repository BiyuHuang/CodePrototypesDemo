package com.wallace.redis

import java.io.Closeable
import java.lang.reflect.Field
import java.util
import java.util.concurrent.atomic.AtomicBoolean

import redis.clients.jedis._
import redis.clients.jedis.exceptions.{JedisMovedDataException, JedisRedirectionException}
import redis.clients.util.{JedisClusterCRC16, SafeEncoder}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

/**
  * Created by wallace on 2020/4/25.
  */
class JedisClusterPipeline(jedisCluster: JedisCluster) extends PipelineBase with Closeable {
  private def getField(cls: Class[_], fieldName: String) = {
    try {
      val field = cls.getDeclaredField(fieldName)
      field.setAccessible(true)
      field
    } catch {
      case e@(_: SecurityException | _: NoSuchFieldException) =>
        throw new RuntimeException(s"can't find or access field '$fieldName' from ${cls.getName}", e)
    }
  }

  private def getValue[T](obj: AnyRef, field: Field): AnyRef = {
    try {
      field.get(obj)
    } catch {
      case e@(_: IllegalAccessException | _: IllegalArgumentException) =>
        throw new RuntimeException("failed to get value", e)
    }
  }

  private val FIELD_CONNECTION_HANDLER: Field = getField(classOf[BinaryJedisCluster], "connectionHandler")
  private val FIELD_CACHE: Field = getField(classOf[JedisClusterConnectionHandler], "cache")
  private val clients: util.LinkedList[Client] = new util.LinkedList[Client]()
  private val jedisMap: util.HashMap[JedisPool, Jedis] = new util.HashMap[JedisPool, Jedis]()
  private val hasDataInBuf: AtomicBoolean = new AtomicBoolean(false)
  private val connectionHandler: JedisSlotBasedConnectionHandler = getValue(jedisCluster, FIELD_CONNECTION_HANDLER)
    .asInstanceOf[JedisSlotBasedConnectionHandler]
  private val clusterInfoCache: JedisClusterInfoCache = getValue(connectionHandler, FIELD_CACHE)
    .asInstanceOf[JedisClusterInfoCache]

  private def getJedis(slot: Int): Jedis = {
    val pool: JedisPool = clusterInfoCache.getSlotPool(slot)
    val tryGetJedis: Option[Jedis] = Option(jedisMap.get(pool))
    val jedisCli: Jedis = if (tryGetJedis.isEmpty) {
      val tmp: Jedis = pool.getResource
      jedisMap.put(pool, tmp)
      tmp
    } else {
      tryGetJedis.get
    }
    hasDataInBuf.set(true)
    jedisCli
  }


  private def flushCachedData(jedis: Jedis): Unit = {
    try {
      jedis.getClient.getAll()
    } catch {
      case e: RuntimeException =>
        e.printStackTrace()
    }
  }

  private def innerSync: Array[Any] = {
    val responseList: ArrayBuffer[Any] = new ArrayBuffer[Any]()
    val clientSet: util.HashSet[Client] = new util.HashSet[Client]()
    var isExcept: Boolean = false
    try {
      isExcept = true
      val clientIter: util.Iterator[Client] = clients.iterator()
      while (clientIter.hasNext) {
        val client: Client = clientIter.next()
        val data: Any = generateResponse(client.getOne).get()
        responseList.append(data)
        if (clientSet.size() != jedisMap.size()) clientSet.add(client)
      }
      isExcept = false
    } catch {
      case je: JedisRedirectionException =>
        if (je.isInstanceOf[JedisMovedDataException]) refreshCluster()
        throw je
    } finally {
      if (isExcept) {
        if (clientSet.size() != jedisMap.size()) {
          val jedisIter1: util.Iterator[Jedis] = jedisMap.values().iterator()
          while (jedisIter1.hasNext) {
            val jedis: Jedis = jedisIter1.next()
            if (!clientSet.contains(jedis.getClient)) flushCachedData(jedis)
          }
        }
        hasDataInBuf.set(false)
        close()
      }
    }

    if (clientSet.size() != jedisMap.size()) {
      val jedisIter2: util.Iterator[Jedis] = jedisMap.values().iterator()
      while (jedisIter2.hasNext) {
        val jedis: Jedis = jedisIter2.next()
        if (!clientSet.contains(jedis.getClient)) flushCachedData(jedis)
      }
    }
    hasDataInBuf.set(false)
    close()
    responseList.result().toArray
  }

  override def getClient(key: String): Client = {
    val binaryKey: Array[Byte] = SafeEncoder.encode(key)
    getClient(binaryKey)
  }

  override def getClient(key: Array[Byte]): Client = {
    val jedis: Jedis = getJedis(JedisClusterCRC16.getSlot(key))
    val client: Client = jedis.getClient
    clients.add(client)
    client
  }

  override def close(): Unit = {
    clean()
    clients.clear()
    jedisMap.values().asScala.foreach {
      jedis =>
        if (hasDataInBuf.get()) flushCachedData(jedis)
        try {
          jedis.close()
        } catch {
          case e: RuntimeException =>
            e.printStackTrace()
        }
    }
  }

  def syncAndReturnAll: Array[Any] = innerSync

  def refreshCluster(): Unit = connectionHandler.renewSlotCache()

  def pipelineSetEx(data: Array[KVDataEX]): Unit = {
    try {
      data.foreach {
        elem =>
          val expireTime: Int = elem.expireTime
          setex(elem.key, expireTime, elem.value)
      }
      syncAndReturnAll
    } catch {
      case e: Exception =>
        throw new RuntimeException("[setex] operator error", e)
    }
  }

  def pipelineHGetAll(keys: Array[String]): Array[KVData] = {
    val result: ArrayBuffer[KVData] = new ArrayBuffer[KVData]()
    try {
      keys.foreach(hgetAll)
      val res = syncAndReturnAll.map(_.asInstanceOf[util.Map[String, String]])

      res.foreach {
        elem =>
          val kv: KVData = KVData(elem.keySet().asScala.head, elem.values().asScala.head)
          result.append(kv)
      }
    } catch {
      case e: Exception =>
        throw new RuntimeException("[setex] operator error", e)
    }

    result.result().toArray[KVData]
  }
}

case class KVData(key: String, value: String)

case class KVDataEX(key: String, value: String, expireTime: Int)

package com.wallace.demo.app.builderdemo

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import com.wallace.demo.app.common.{LogSupport, Using}
import com.wallace.demo.app.utils.SystemEnvUtils

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Created by 10192057 on 2018/6/4 0004.
  */
object BuildObjDemo {
  def builder(): Builder = new Builder

  private val activeThreadObj: InheritableThreadLocal[BuildObjDemo] = new InheritableThreadLocal[BuildObjDemo]

  private val defaultObj: AtomicReference[BuildObjDemo] = new AtomicReference[BuildObjDemo]

  class Builder extends Using {
    private[this] val options: mutable.HashMap[String, String] = new mutable.HashMap[String, String]

    def getOrCreate(): BuildObjDemo = synchronized {
      var obj: BuildObjDemo = activeThreadObj.get()
      if ((obj ne null) && !obj.isStopped) {
        return obj
      }
      BuildObjDemo.synchronized {
        obj = defaultObj.get()
        if ((obj ne null) && !obj.isStopped) {
          return obj
        }

        require(options.nonEmpty, "Builder configuration must not be empty.")
        obj = new BuildObjDemo(options.getOrElse("demo.master", "default"), options)
        defaultObj.set(obj)
      }
      return obj
    }

    def master(master: String): Builder = config("demo.master", master)

    def config(key: String, value: String): Builder = synchronized {
      options += key -> value
      this
    }

    def config(conf: ObjConf): Builder = synchronized {
      conf.getAll.foreach { case (k, v) => options += k -> v }
      this
    }
  }

}

class BuildObjDemo(@transient private val demoName: String, private val conf: mutable.HashMap[String, String]) {

  private val stopped: AtomicBoolean = new AtomicBoolean(false)

  def isStopped: Boolean = stopped.get()

  @transient
  private val initialSessionOptions = new scala.collection.mutable.HashMap[String, String]

  @transient lazy val demoConf: mutable.HashMap[String, String] = initialSessionOptions
}

class ObjConf(loadDefaults: Boolean) extends Cloneable with LogSupport with Serializable {

  def this() = this(true)

  private val settings = new ConcurrentHashMap[String, String]()

  if (loadDefaults) {
    loadFromSystemProperties(false)
  }

  private def loadFromSystemProperties(silent: Boolean): ObjConf = {
    // Load any spark.* system properties
    for ((key, value) <- SystemEnvUtils.getEnvs.asScala if key.startsWith("obj.")) {
      set(key, value, silent)
    }
    this
  }

  private def set(key: String, value: String, silent: Boolean): ObjConf = {
    if (key == null) {
      throw new NullPointerException("null key")
    }
    if (value == null) {
      throw new NullPointerException("null value for " + key)
    }
    if (!silent) {
      logger.warn(s"The configuration key '$key' has been deprecated.")
    }
    settings.put(key, value)
    this
  }

  def set(key: String, value: String): ObjConf = {
    set(key, value, silent = false)
  }

  def getOption(key: String): Option[String] = {
    Option(settings.get(key)).orElse(None)
  }

  def get(key: String): String = {
    getOption(key).getOrElse(throw new NoSuchElementException(key))
  }

  def getAll: Array[(String, String)] = {
    settings.entrySet().asScala.map(x => (x.getKey, x.getValue)).toArray
  }
}

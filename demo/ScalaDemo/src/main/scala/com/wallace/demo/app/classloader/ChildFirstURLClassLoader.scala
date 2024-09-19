package com.wallace.demo.app.classloader

import java.net.URL
import java.util

import scala.collection.JavaConverters._

/**
  * com.wallace.demo.app.classloader
  * Created by 10192057 on 2018/1/12 0012.
  */
class ChildFirstURLClassLoader(urls: Array[URL], parent: ClassLoader) extends MutableURLClassLoader(urls, null) {
  private val parentClassLoader = new ParentClassLoader(parent)

  override def loadClass(name: String, resolve: Boolean): Class[_] = {
    try {
      super.loadClass(name, resolve)
    } catch {
      case e: ClassNotFoundException =>
        parentClassLoader.loadClass(name, resolve)
    }
  }

  override def getResource(name: String): URL = {
    val url = super.findResource(name)
    val res = if (url != null) url else parentClassLoader.getResource(name)
    res
  }

  override def getResources(name: String): util.Enumeration[URL] = {
    val childUrls = super.findResources(name).asScala
    val parentUrls = parentClassLoader.getResources(name).asScala
    (childUrls ++ parentUrls).asJavaEnumeration
  }

  override def addURL(url: URL): Unit = {
    super.addURL(url)
  }

}

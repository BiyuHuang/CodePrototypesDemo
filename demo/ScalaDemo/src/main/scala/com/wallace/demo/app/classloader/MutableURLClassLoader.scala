package com.wallace.demo.app.classloader

import java.net.{URL, URLClassLoader}

/**
  * com.wallace.demo.app.classloader
  * Created by 10192057 on 2018/1/12 0012.
  */
private[classloader] class MutableURLClassLoader(urls: Array[URL], parent: ClassLoader) extends URLClassLoader(urls, parent) {

  override def addURL(url: URL): Unit = {
    super.addURL(url)
  }

  override def getURLs: Array[URL] = {
    super.getURLs
  }

}

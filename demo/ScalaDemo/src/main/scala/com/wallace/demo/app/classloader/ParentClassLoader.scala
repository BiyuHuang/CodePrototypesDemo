package com.wallace.demo.app.classloader

/**
  * com.wallace.demo.app.classloader
  * Created by 10192057 on 2018/1/12 0012.
  */
private[classloader] class ParentClassLoader(parent: ClassLoader) extends ClassLoader(parent) {
  override def findClass(name: String): Class[_] = {
    super.findClass(name)
  }

  override def loadClass(name: String): Class[_] = {
    super.loadClass(name)
  }

  override def loadClass(name: String, resolve: Boolean): Class[_] = {
    super.loadClass(name, resolve)
  }
}

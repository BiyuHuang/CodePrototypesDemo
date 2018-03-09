package com.wallace.demo.app.classloader

import java.io.{Closeable, File}
import java.net.{URI, URISyntaxException, URL}

import com.wallace.demo.app.common.Using
import com.wallace.demo.app.utils.SystemEnvUtils
import com.zte.hadooploader.decompression.DecompressionFactory

/**
  * com.wallace.demo.app.classloader
  * Created by 10192057 on 2018/1/12 0012.
  */
object DemoClassLoader extends Using {
  private var mainClass: Class[_] = _

  def tryWithResource[R <: Closeable, T](createResource: => R)(f: R => T): T = {
    val resource = createResource
    try f.apply(resource) finally resource.close()
  }


  def addJarToClasspath(localJar: String, loader: MutableURLClassLoader): Unit = {
    val uri = resolveURI(localJar)
    uri.getScheme match {
      case "file" | "local" =>
        val file = new File(uri.getPath)
        if (file.exists()) {
          loader.addURL(file.toURI.toURL)
        } else {
          log.warn(s"Local jar $file does not exist, skipping.")
        }
      case _ =>
        log.warn(s"Skip remote jar $uri.")
    }
  }

  def resolveURI(path: String): URI = {
    val absolutePath = new File(path).getAbsoluteFile.toURI.getPath
    try {
      val uri = new URI(absolutePath)
      if (uri.getScheme != null) {
        return uri
      }
      // make sure to handle if the path has a fragment (applies to yarn
      // distributed cache)
      if (uri.getFragment != null) {
        val absoluteURI = new File(uri.getPath).getAbsoluteFile.toURI
        return new URI(absoluteURI.getScheme, absoluteURI.getHost, absoluteURI.getPath, uri.getFragment)
      }
    } catch {
      case _: URISyntaxException =>
    }
    new File(absolutePath).toURI
  }

  private val loader: MutableURLClassLoader = if (false) {
    new ChildFirstURLClassLoader(new Array[URL](0),
      Thread.currentThread.getContextClassLoader)
  } else {
    new MutableURLClassLoader(new Array[URL](0),
      Thread.currentThread.getContextClassLoader)
  }

  def classForName(className: String): Class[_] = {
    Class.forName(className, true, getContextOrClassLoader)
    // scalastyle:on classforname
  }

  def getClassLoader: ClassLoader = getClass.getClassLoader

  def getContextOrClassLoader: ClassLoader =
    Option(Thread.currentThread().getContextClassLoader).getOrElse(getClassLoader)


  def main(args: Array[String]): Unit = {
    Thread.currentThread.setContextClassLoader(loader)
    val rootPath: String = SystemEnvUtils.getUserDir
    addJarToClasspath(s"$rootPath/lib/loader.jar", loader)
    mainClass = classForName("com.zte.hadooploader.decompression.DecompressionFactory")
    val instance: DecompressionFactory = if (classOf[DecompressionFactory].isAssignableFrom(mainClass)) {
      mainClass.newInstance().asInstanceOf[DecompressionFactory]
    } else {
      if (classOf[scala.App].isAssignableFrom(mainClass)) {
        log.warn("Subclasses of scala.App may not work correctly. Use a new Object instead.")
      }
      new DecompressionFactory()
    }

    val res = instance.getDecompression("test.zip")

    log.info(res.getClass.getName.stripSuffix("$"))
  }
}

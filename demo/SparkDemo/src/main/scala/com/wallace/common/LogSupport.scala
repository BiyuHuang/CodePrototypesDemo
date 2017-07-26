package com.wallace.common

import org.apache.log4j.Logger

/**
  * Created by Wallace on 2016/5/2.
  */
trait LogSupport extends Serializable {

  //1.log4j.properties配置
  //  val rootPath = new File("").getAbsolutePath.replaceAll("\\\\", "/")
  //  PropertyConfigurator.configure(rootPath + "/demo/SparkDemo/src/main/resources/log4j.properties")
  //  protected val log = Logger.getLogger(this.getClass)

  //2.logback.xml配置
  //    protected val lc: LoggerContext = new LoggerContext()
  //    val configurator = new JoranConfigurator()
  //    configurator.setContext(lc)
  //    configurator.doConfigure(".\\src\\main\\resources\\logback.xml")
  //    lc.reset()
  //    protected val log = lc.getLogger(this.getClass)

  //3.默认日志配置
  protected val log: Logger = Logger.getLogger(this.getClass)
}

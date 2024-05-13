package com.wallace.demo.app.patterns.commanddemo

import scala.collection.mutable.ArrayBuffer

/**
 * Author: biyu.huang
 * Date: 2024/5/13 16:57
 * Description:
 */
class Manager {
  private val commands: ArrayBuffer[Command] = new ArrayBuffer[Command]()

  def addCommand(command: Command): Unit = this.commands.append(command)

  def executeCommand(): Unit = {
    commands.foreach {
      cmd =>
        cmd.execute()
    }
    commands.clear()
  }
}

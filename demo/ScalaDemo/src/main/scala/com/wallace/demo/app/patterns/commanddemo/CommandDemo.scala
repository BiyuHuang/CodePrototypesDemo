package com.wallace.demo.app.patterns.commanddemo

/**
 * Author: biyu.huang
 * Date: 2024/5/13 16:26
 * Description: Command Pattern
 */
object CommandDemo {
  def main(args: Array[String]): Unit = {
    val game = new Game("dummy game")
    val publishCommand: PublishCommand = new PublishCommand(game)
    val closeCommand: CloseCommand = new CloseCommand(game)

    val manager = new Manager()
    manager.addCommand(publishCommand)
    manager.addCommand(closeCommand)
    manager.executeCommand()
  }
}

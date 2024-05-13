package com.wallace.demo.app.patterns.commanddemo

/**
 * Author: biyu.huang
 * Date: 2024/5/13 16:52
 * Description:
 */
class PublishCommand(game: Game) extends Command {
  override def execute(): Unit = {
    this.game.publish()
  }
}

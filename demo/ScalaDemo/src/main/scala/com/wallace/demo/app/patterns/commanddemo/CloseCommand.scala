package com.wallace.demo.app.patterns.commanddemo

/**
 * Author: biyu.huang
 * Date: 2024/5/13 16:54
 * Description:
 */
class CloseCommand(game: Game) extends Command {
  override def execute(): Unit = {
    game.close()
  }
}

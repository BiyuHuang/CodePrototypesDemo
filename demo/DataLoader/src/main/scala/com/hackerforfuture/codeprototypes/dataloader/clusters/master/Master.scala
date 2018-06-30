/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hackerforfuture.codeprototypes.dataloader.clusters.master

import akka.actor.Actor

/**
  * Created by wallace on 2018/6/23.
  */
class Master(host: String, port: Int) extends Actor {

  // TODO Cluster : Manager Node , Controll Node
  override def receive: Receive = {
    //TODO Message Handler
    case 0 =>
    case 1 =>
    case 2 =>

  }
}

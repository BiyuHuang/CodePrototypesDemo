package com.wallace.demo.app.algorithmdemo.linkedlist

import com.wallace.demo.app.common.LogSupport

/**
 * Author: biyu.huang
 * Date: 2023/5/25 11:02
 * Description:
 */
class LinkedList[T]() {
  @transient private var first: Node[T] = _
  @transient private var last: Node[T] = _
  @transient private var size: Int = 0
  @transient private var modCount: Int = 0

  class Node[U](var item: U, var preNode: Node[U], var nextNode: Node[U]) {
    override def toString: String = {
      val preNodeToStr = if (preNode == null) "null" else preNode.item
      val nextNodeToStr = if (nextNode == null) "null" else nextNode.item
      s"Node(item=$item, preNode=$preNodeToStr, nextNode=$nextNodeToStr)"
    }
  }

  def getFirst: Node[T] = {
    this.first
  }

  def getLast: Node[T] = {
    this.last
  }

  def addNode(e: T): Boolean = {
    val l: Node[T] = this.last
    val newNode: Node[T] = new Node(e, l, null)
    this.last = newNode
    if (l == null) {
      this.first = newNode
    } else {
      l.nextNode = newNode
    }
    this.size += 1
    this.modCount += 1
    true
  }

  def removeNode(e: T): Boolean = {
    var node: Node[T] = this.getFirst
    var flag: Boolean = true
    while (node != null && flag) {
      if (node.item == e) {
        val next: Node[T] = node.nextNode
        val pre: Node[T] = node.preNode
        if (pre == null) {
          this.first = next
        } else {
          pre.nextNode = next
          node.preNode = null
        }

        if (next == null) {
          this.last = pre
        } else {
          next.preNode = pre
          node.nextNode = null
        }
        node.item = null.asInstanceOf[T]
        this.size -= 1
        this.modCount -= 1
        flag = false
      } else {
        node = node.nextNode
      }
    }
    !flag
  }

  def getNode(index: Int): Node[T] = {
    if (index < (this.size >> 1)) {
      var node: Node[T] = this.getFirst
      var j: Int = 0
      while (j < index) {
        node = node.nextNode
        j += 1
      }
      node
    } else {
      var node: Node[T] = this.getLast
      var i: Int = this.size - 1
      while (i > index) {
        node = node.preNode
        i -= 1
      }
      node
    }
  }

  def addAll(elements: Iterable[T]): Boolean = addAll(this.size, elements)

  def addAll(index: Int, elements: Iterable[T]): Boolean = {
    if (elements.isEmpty) {
      false
    } else {
      var pre: Node[T] = null
      var cur: Node[T] = null
      if (index == size) {
        pre = this.getLast
      } else {
        cur = getNode(index)
        pre = cur.preNode
      }
      elements.foreach {
        e =>
          val newNode = new Node(e, pre, null)
          if (pre == null) {
            this.first = newNode
          } else {
            pre.nextNode = newNode
          }
          pre = newNode
      }
      if (cur == null) {
        this.last = pre
      } else {
        pre.nextNode = cur
        cur.preNode = pre
      }
      size += elements.size
      modCount += 1
      true
    }
  }
}

object Boot extends LogSupport {
  def main(args: Array[String]): Unit = {
    val root: LinkedList[String] = new LinkedList[String]()
    root.addAll(Array("node_001", "node_002", "node_003", "node_004", "node_005"))
    //    root.addNode("node_001")
    //    root.addNode("node_002")
    //    root.addNode("node_003")
    //    root.addNode("node_004")
    //    root.addNode("node_005")
    logger.info(s"first => ${root.getFirst}, last => ${root.getLast}")
    root.removeNode("node_004")
    logger.info(s"first => ${root.getFirst}, last => ${root.getLast}")

    logger.info(root.getNode(2).toString)
  }
}
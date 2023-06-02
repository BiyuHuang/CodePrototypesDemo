package com.wallace.demo.app.algorithmdemo.datastructure.tree

import com.wallace.demo.app.common.LogSupport

import scala.collection.mutable.ArrayBuffer
import scala.util.hashing.MurmurHash3

/**
 * Author: biyu.huang
 * Date: 2023/6/2 16:57
 * Description:
 */
// scalastyle:off
abstract class TreeNode[T, BaseType <: TreeNode[T, BaseType]] {
  // scalastyle:on
  self: BaseType =>
  private var parentNode: Option[BaseType] = None

  private var deep: Int = 0

  private var isEnd: Boolean = false

  private val children: ArrayBuffer[BaseType] = new ArrayBuffer[BaseType]()

  def getContent: T

  def setEnd(end: Boolean): Unit = {
    this.isEnd = end
  }

  def getEnd: Boolean = this.isEnd

  def setDeep(deep: Int): Unit = {
    this.deep = deep
  }

  def getDeep: Int = this.deep

  def addChild(child: BaseType): Boolean = {
    child.setParentNode(this)
    child.setDeep(this.getDeep + 1)
    this.children.append(child)
    true
  }

  def setChildren(children: ArrayBuffer[BaseType]): Boolean = {
    children.forall {
      child =>
        this.addChild(child)
    }
  }

  def findNode(n: BaseType): Option[BaseType] = this.getChildren.find(x => x.equals(n))

  def getChildren: ArrayBuffer[BaseType] = this.children

  def setParentNode(parent: BaseType): Unit = {
    this.parentNode = Option(parent)
  }

  def getParentNode: Option[BaseType] = this.parentNode

  override def toString: String = {
    s"TreeNode{isEnd=${this.getEnd}, deep=${this.getDeep}, content=${this.getContent}, " +
      s"children=[${this.getChildren.map(x => s"${x.toString}").mkString(",")}]}"
  }

  def treeString(depth: Int): String = {
    var prefix = "\t"
    if (depth > 0) {
      prefix = "\t" * (depth + 1)
    }
    val childStr = this.getChildren.map(x => s"\n$prefix${x.treeString(depth + 1)}").mkString(",")
    s"TreeNode{isEnd=${this.getEnd}, deep=${this.getDeep}, content=${this.getContent}, " +
      s"children=[$childStr]}"

  }
}

class TrieTree {
  private val root = new TrieTreeNode(None)


  override def toString: String = this.root.treeString(0)

  def addWord(word: String): Unit = {
    var curNode = root
    word.toCharArray.foreach {
      ch =>
        val node = new TrieTreeNode(Option(ch))
        if (curNode.findNode(node).isDefined) {
          curNode = curNode.findNode(node).get
        } else {
          curNode.addChild(node)
          curNode = node
        }
    }
    curNode.setEnd(true)
  }

  def hasWord(word: String): Boolean = ???

  def removeWord(word: String): Boolean = ???

  def visitNode: String = ???

  def getPrefix: String = ???

  class TrieTreeNode(content: Option[Char]) extends TreeNode[Char, TrieTreeNode] {
    override def hashCode(): Int = {
      MurmurHash3.arrayHash(Array(content.getOrElse(""), this.getDeep))
    }

    override def equals(obj: Any): Boolean = {
      if (obj == null) {
        false
      } else {
        this.getContent == obj.asInstanceOf[TrieTreeNode].getContent
      }
    }

    override def getContent: Char = {
      this.content.getOrElse(null.asInstanceOf[Char])
    }
  }
}

object TrieTree extends LogSupport {
  def main(args: Array[String]): Unit = {
    val data: Array[String] = Array("flow", "flower", "flight", "")
    val trieTree: TrieTree = new TrieTree()
    data.foreach {
      w =>
        trieTree.addWord(w)
    }
    logger.info(trieTree.toString)
  }
}
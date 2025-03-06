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

  private def setDeep(deep: Int): Unit = {
    this.deep = deep
  }

  def getDeep: Int = this.deep

  def addChild(child: BaseType): Boolean = {
    child.setParentNode(this)
    child.setDeep(this.getDeep + 1)
    this.children.append(child)
    true
  }

  def removeChild(child: BaseType): Boolean = {
    if (this.children.contains(child)) {
      this.children.-=(child)
      true
    } else {
      false
    }
  }


  def setChildren(children: ArrayBuffer[BaseType]): Boolean = {
    children.forall {
      child =>
        this.addChild(child)
    }
  }

  def findNode(n: BaseType): Option[BaseType] = this.getChildren.find(x => x.equals(n))

  def getChildren: ArrayBuffer[BaseType] = this.children

  private def setParentNode(parent: BaseType): Unit = {
    this.parentNode = Option(parent)
  }

  def getParentNode: Option[BaseType] = this.parentNode

  override def toString: String = {
    s"TreeNode{isEnd=${this.getEnd}, deep=${this.getDeep}, content=${this.getContent}, " +
      s"children=[${this.getChildren.map(x => s"${x.toString}").mkString(",")}]}"
  }

  def treeString(depth: Int, prefix: String = "+- ", isLastNode: Boolean = false): String = {
    val currentPrefix: String = if (depth > 0) {
      val sep = if (this.getParentNode.isDefined && this.getParentNode.get.getChildren.size <= 1) {
        " "
      } else {
        if (isLastNode) " " else "|"
      }
      prefix.replace("+- ", "") + sep + (" " * (depth + 1)) + "+- "
    } else {
      prefix
    }
    val nodeNum: Int = this.getChildren.size
    val childStr: String = this.getChildren.zipWithIndex.map {
      case (x, index) =>
        val isLastNode = if (nodeNum > 1 && index == (nodeNum - 1)) true else false
        s"\n$currentPrefix${x.treeString(depth + 1, currentPrefix, isLastNode)}"
    }.mkString(",")
    s"TreeNode{isEnd=${this.getEnd}, deep=${this.getDeep}, content=${this.getContent}, " +
      s"children=[$childStr]}"
  }
}

class TrieTree {
  private val root: TrieTreeNode = new TrieTreeNode(None)


  override def toString: String = this.root.treeString(0)

  def addWord(word: String): Unit = {
    var curNode = root
    word.toCharArray.foreach {
      ch =>
        val node: TrieTreeNode = new TrieTreeNode(Option(ch))
        val foundNode: Option[TrieTreeNode] = curNode.findNode(node)
        if (foundNode.isDefined) {
          curNode = foundNode.get
        } else {
          curNode.addChild(node)
          curNode = node
        }
    }
    curNode.setEnd(true)
  }

  def hasWord(word: String): Boolean = {
    var curNode: TrieTreeNode = root
    word.toCharArray.foreach {
      ch =>
        val node: TrieTreeNode = new TrieTreeNode(Option(ch))
        if (curNode.findNode(node).isDefined) {
          curNode = curNode.findNode(node).get
        } else {
          return false
        }
    }
    curNode.getEnd
  }

  def removeWord(word: String): Boolean = {
    if (hasWord(word)) {
      var curNode: TrieTreeNode = root
      word.toCharArray.foreach {
        ch =>
          val node: TrieTreeNode = new TrieTreeNode(Option(ch))
          curNode = curNode.findNode(node).get
      }
      curNode.setEnd(false)
      if (curNode.getChildren.isEmpty) {
        // no children
        var parentNode: Option[TrieTreeNode] = curNode.getParentNode
        while (parentNode.isDefined) {
          if (parentNode.get.getChildren.size == 1 && !parentNode.get.getEnd) {
            parentNode.get.removeChild(curNode)
            curNode = parentNode.get
            parentNode = curNode.getParentNode
          } else {
            parentNode.get.removeChild(curNode)
            return true
          }
        }
      }
      true
    } else {
      false
    }
  }

  def visitNode: String = ???

  def getCommonPrefix: String = ???

  private class TrieTreeNode(content: Option[Char]) extends TreeNode[Char, TrieTreeNode] {
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
    val data: Array[String] = Array("flow", "flower", "florida", "flight", "world", "worry")
    val trieTree: TrieTree = new TrieTree()
    data.foreach {
      w =>
        trieTree.addWord(w)
    }
    logger.info(trieTree.toString)

    logger.info("word isn't exists: " + trieTree.hasWord("word").toString)
    logger.info("florida is exists: " + trieTree.hasWord("florida").toString)

    trieTree.removeWord("florida")
    logger.info(trieTree.toString)
  }
}

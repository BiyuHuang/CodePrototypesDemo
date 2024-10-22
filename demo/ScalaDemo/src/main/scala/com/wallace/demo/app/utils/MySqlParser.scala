package com.wallace.demo.app.utils

import com.wallace.demo.app.common.LogSupport

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.control.NonFatal

/**
 * Author: biyu.huang
 * Date: 2024/10/22 09:51
 * Description: Parse MySQL CREATE TABLE statements based on AST(Abstract Syntax Tree).
 */
// 基础的 Token 特征
sealed trait Token {
  override def toString: String = this match {
    case symbol: Symbol => symbol.value
    case number: NumberLiteral => number.value.toString
    case str: StringLiteral => str.value
    case dataType: DataType => dataType.value
    case id: Identifier => id.value
    case kw: Keyword => kw.value
    case _ => super.toString
  }
}

// 具体的 Token 类型
case class Keyword(value: String) extends Token

case class DataType(value: String) extends Token

case class Identifier(value: String) extends Token

case class NumberLiteral(value: Int) extends Token

case class StringLiteral(value: String) extends Token

case class Symbol(value: String) extends Token

case object EOF extends Token // 结束标记

sealed trait Constraint

case object PrimaryKey extends Constraint

case object NotNull extends Constraint

case object AutoIncrement extends Constraint

case class Default(value: String) extends Constraint

case class CharacterSet(value: String) extends Constraint

case class Collate(value: String) extends Constraint

case class FieldType(
  baseType: String,
  fullDataType: Option[String] = None
)

sealed trait AST

case class ColumnDefinition(
  columnName: String,
  dataType: String,
  constraints: List[Constraint] = List(),
  comment: Option[String] = None,
  isUnsigned: Boolean = false, // 添加无符号标志
  isZeroFill: Boolean = false
) extends AST

case class IndexDefinition(
  indexName: Option[String],
  columns: List[String],
  isUnique: Boolean = false
) extends AST

case class TableDefinition(
  tableName: String,
  isTemporary: Boolean,
  columns: List[ColumnDefinition],
  indexes: List[IndexDefinition] = List()
) extends AST

class MySqlParser(tokens: List[Token]) extends LogSupport {
  private var currentPosition = 0

  private def consume(): Token = {
    val token = tokens(currentPosition)
    currentPosition += 1
    token
  }

  private def peek(): Token = tokens(currentPosition)

  def parse(): TableDefinition = {
    try {
      expect(Keyword("CREATE"))
      val isTemporary = if (peek() == Keyword("TEMPORARY")) {
        consume() // consume TEMPORARY
        true
      } else {
        false
      }
      expect(Keyword("TABLE"))
      if (peek() == Keyword("IF")) {
        expect(Keyword("IF")) // consume IF
        expect(Keyword("NOT")) // consume NOT
        expect(Keyword("EXISTS")) // consume EXISTS
      }
      val tableName: String = consume().asInstanceOf[Identifier].value
      expect(Symbol("("))

      val columns: ListBuffer[ColumnDefinition] = mutable.ListBuffer[ColumnDefinition]()
      val indexes: ListBuffer[IndexDefinition] = mutable.ListBuffer[IndexDefinition]()

      while (peek() != Symbol(")")) {
        if (peek() == Keyword("INDEX") || peek() == Keyword("UNIQUE")) {
          indexes += parseIndexDefinition()
        } else {
          columns += parseColumnDefinition()
        }
        if (peek() == Symbol(",")) consume() // consume comma
      }
      expect(Symbol(")"))
      while (peek() != EOF) {
        logger.info(s"skip to parse token: ${peek()}")
        consume()
      }
      // expect(Symbol(";"))

      TableDefinition(tableName, isTemporary, columns.toList, indexes.toList)
    } catch {
      case NonFatal(e) =>
        logger.error(s"failed to parse ${peek()}, caused by: ", e)
        throw e
    }
  }

  private def expectDataType(): String = {
    peek() match {
      case DataType(dt) =>
        consume() // Consume the data type token
        dt
      case other => throw new RuntimeException(s"Expected data type: ${other.toString}")
    }
  }

  private def parseDataType(): FieldType = {
    val baseType = expectDataType()
    if (peek() == Symbol("(")) {
      consume() // Consume '('
      val params = new mutable.StringBuilder
      while (peek() != Symbol(")")) {
        params.append(consume().toString)
      }
      consume() // Consume ')'
      FieldType(baseType, Some(s"$baseType($params)"))
    } else {
      FieldType(baseType)
    }
  }

  private def parseColumnDefinition(): ColumnDefinition = {
    val columnName: String = consume().asInstanceOf[Identifier].value
    val fieldType: FieldType = parseDataType()
    val dataType: String = fieldType.fullDataType.getOrElse(fieldType.baseType)
    var isUnsigned: Boolean = false
    var isZeroFill: Boolean = false

    // 检查是否为无符号类型
    if (fieldType.baseType.matches(
      "(?i)(BIGINT|INT|INTEGER|BIGINT|SMALLINT|TINYINT|MEDIUMINT|FLOAT|DOUBLE|DECIMAL)")) {
      logger.info(s"parseColumnDefinition: peek token -> ${peek()}")
      peek() match {
        case Keyword("UNSIGNED") =>
          consume() // consume UNSIGNED
          isUnsigned = true
        case Keyword("ZEROFILL") =>
          consume() // consume UNSIGNED
          isZeroFill = true
        case _ =>
      }
      // throw new Exception(s"UNSIGNED/ZEROFILL is not valid for data type: ${fieldType.baseType}")
    }

    val constraints: List[Constraint] = parseConstraints()
    var comment: Option[String] = None

    if (peek() == Keyword("COMMENT")) {
      consume() // consume COMMENT
      val commentText = consume().asInstanceOf[StringLiteral].value
      comment = Some(commentText)
    }

    ColumnDefinition(columnName, dataType, constraints, comment, isUnsigned, isZeroFill)
  }

  private def parseConstraints(): List[Constraint] = {
    val constraints = mutable.ListBuffer[Constraint]()
    logger.info(s"parseConstraints: peek token -> ${peek()}")
    while (peek() match {
      case Keyword("AUTO_INCREMENT") |
           Keyword("PRIMARY") |
           Keyword("KEY") |
           Keyword("NOT") |
           Keyword("NULL") |
           Keyword("DEFAULT") |
           Keyword("CHARACTER") |
           Keyword("COLLATE") => true
      case _ => false
    }) {
      peek() match {
        case Keyword("PRIMARY") =>
          consume() // consume PRIMARY
          expect(Keyword("KEY"))
          constraints += PrimaryKey
        case Keyword("NOT") =>
          consume() // consume NOT
          expect(Keyword("NULL"))
          constraints += NotNull
        case Keyword("AUTO_INCREMENT") =>
          consume() // consume AUTO_INCREMENT
          constraints += AutoIncrement
        case Keyword("CHARACTER") =>
          consume() // consume CHARACTER
          expect(Keyword("SET"))
          val characterSet = consume().asInstanceOf[Identifier].value
          constraints += CharacterSet(characterSet)
        case Keyword("COLLATE") =>
          consume() // consume COLLATE
          val collate = consume().asInstanceOf[Identifier].value
          constraints += Collate(collate)
        case Keyword("DEFAULT") =>
          consume() // consume DEFAULT
          val defaultValue = consume().asInstanceOf[StringLiteral].value
          constraints += Default(defaultValue)
        case _ =>
      }
    }
    constraints.toList
  }

  private def parseIndexDefinition(): IndexDefinition = {
    var isUnique = false
    if (peek() == Keyword("UNIQUE")) {
      isUnique = true
      consume() // consume UNIQUE
    }
    expect(Keyword("INDEX"))
    // scalastyle:off
    val indexName: Option[String] =
      if (peek().isInstanceOf[Identifier]) Option(consume().asInstanceOf[Identifier].value) else None
    // scalastyle:on
    expect(Symbol("("))

    val columns = scala.collection.mutable.ListBuffer[String]()
    while (peek() != Symbol(")")) {
      columns += consume().asInstanceOf[Identifier].value
      if (peek() == Symbol(",")) consume() // consume comma
    }
    expect(Symbol(")"))

    IndexDefinition(indexName, columns.toList, isUnique)
  }

  private def expect(token: Token): Unit = {
    if (consume() != token) throw new Exception(s"Expected $token but found ${peek()}")
  }
}

object MySqlLexer {
  private final val keywords: Set[String] = Set(
    "CREATE", "TEMPORARY", "TABLE", "IF", "NOT", "EXISTS", "PRIMARY", "KEY", "UNIQUE",
    "FOREIGN", "REFERENCES", "ENGINE", "AUTO_INCREMENT", "COMMENT", "CHARACTER", "SET",
    "COLLATE", "DEFAULT", "NULL", "INDEX", "UNSIGNED"
  )

  private final val dataTypes: Set[String] = Set(
    "TINYINT", "SMALLINT", "MEDIUMINT", "INT", "INTEGER", "BIGINT", "FLOAT", "DOUBLE", "REAL",
    "DECIMAL", "NUMERIC", "BIT", "DATE", "TIME", "DATETIME", "TIMESTAMP", "YEAR",
    "CHAR", "VARCHAR", "BINARY", "VARBINARY", "TINYTEXT", "TEXT", "MEDIUMTEXT", "LONGTEXT",
    "TINYBLOB", "BLOB", "MEDIUMBLOB", "LONGBLOB", "ENUM"
  )

  def tokenize(input: String): List[Token] = {
    val tokens: ListBuffer[Token] = mutable.ListBuffer[Token]()
    val builder: mutable.StringBuilder = new mutable.StringBuilder
    var inString = false
    var inIdentifier = false
    var currentChar: Char = '\u0000'

    def addToken(): Unit = {
      val word = builder.toString.trim
      if (word.nonEmpty) {
        val token = word.toUpperCase match {
          case kw if keywords.contains(kw) => Keyword(kw)
          case dt if dataTypes.contains(dt) => DataType(dt)
          case num if num.matches("\\d+") => NumberLiteral(num.toInt)
          case str if str.startsWith("'") && str.endsWith("'") =>
            StringLiteral(word.stripPrefix("'").stripSuffix("'"))
          case id if id.startsWith("`") && id.endsWith("`") =>
            Identifier(word.stripPrefix("`").stripSuffix("`"))
          case _ => Identifier(word)
        }
        tokens += token
      }
      builder.clear()
    }

    for (i <- input.indices) {
      currentChar = input(i)
      currentChar match {
        case '\'' if inString =>
          // End of string
          builder.append(currentChar)
          inString = false
          addToken()
        case '\'' =>
          // Start of string
          inString = true
          builder.append(currentChar)
        case '`' if inIdentifier =>
          // End of identifier
          builder.append(currentChar)
          inIdentifier = false
          addToken()
        case '`' =>
          // Start of identifier
          inIdentifier = true
          builder.append(currentChar)
        case ',' if !inString =>
          // Treat as a symbol when not inside a string
          addToken()
          tokens += Symbol(",")
        case '(' | ')' | ';' if !inString =>
          // Treat as symbols
          addToken()
          tokens += Symbol(currentChar.toString)
        case ' ' | '\n' | '\t' if !inString =>
          // Treat as token delimiter when not in string
          addToken()
        case _ =>
          // Regular character, add to builder
          builder.append(currentChar)
      }
    }

    // Add any remaining token
    addToken()
    tokens += EOF
    tokens.toList
  }
}

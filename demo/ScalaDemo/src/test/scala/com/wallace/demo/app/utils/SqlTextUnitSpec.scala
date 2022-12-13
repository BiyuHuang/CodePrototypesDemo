/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.utils

import com.wallace.demo.app.UnitSpec

/**
  * com.wallace.demo.app.utils
  * Created by 10192057 on 2018/2/22 0022.
  */
class SqlTextUnitSpec extends UnitSpec with SqlText {
  teamID should "do unit test for insertIntoTabSql" in {
    val sqlMetaData: SqlMetaData = SqlMetaData("textfile_origin_tab", "tgt_tab", "col1,col2,col3",
      Some("p_provincecode=999999,p_date=\'2018-02-21\'"), Some("p_provincecode=999999 and p_date=\'2018-02-21\'"))
    val res = insertIntoTabSql(sqlMetaData)
    logger.info(s"InsertIntoTabSql: $res")

    res shouldBe
      s"""
         |INSERT INTO TABLE tgt_tab PARTITION(p_provincecode=999999,p_date='2018-02-21')
         |SELECT col1,col2,col3 FROM textfile_origin_tab
         |WHERE p_provincecode=999999 and p_date='2018-02-21'
         |""".stripMargin
  }

  teamID should "do unit test for insertIntoTabSql: no partition and no condition" in {
    val sqlMetaData: SqlMetaData = SqlMetaData("textfile_origin_tab", "tgt_tab", "col1,col2,col3", None, None)
    val res = insertIntoTabSql(sqlMetaData)
    logger.info(s"InsertIntoTabSql: $res")

    res shouldBe
      s"""
         |INSERT INTO TABLE tgt_tab \nSELECT col1,col2,col3 FROM textfile_origin_tab\n
         |""".stripMargin
  }

  teamID should "do unit test for selectFieldsSql" in {
    val sqlMetaData: SqlMetaData = SqlMetaData("textfile_origin_tab", "tgt_tab", "col1,col2,col3",
      Some("p_provincecode=999999,p_date=\'2018-02-21\'"), Some("p_provincecode=999999 and p_date=\'2018-02-21\'"))
    val res = selectFieldsSql(sqlMetaData)
    logger.info(
      s"""|SelectFieldsSql:
          |$res""".stripMargin)

    res shouldBe
      s"""|SELECT col1,col2,col3 FROM textfile_origin_tab
          |WHERE p_provincecode=999999 and p_date='2018-02-21'""".stripMargin
  }
}

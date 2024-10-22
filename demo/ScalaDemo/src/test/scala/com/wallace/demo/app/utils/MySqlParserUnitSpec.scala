package com.wallace.demo.app.utils

import com.wallace.demo.app.UnitSpec

/**
 * Author: biyu.huang
 * Date: 2024/10/22 10:35
 * Description:
 */
class MySqlParserUnitSpec extends UnitSpec {
  teamID should "do unit test for MySQLParser" in {
    val createTableSQL: String =
      """
      CREATE TABLE IF NOT EXISTS products (
          `auto_id` bigint(20) NOT NULL AUTO_INCREMENT,
          product_id INT UNSIGNED PRIMARY KEY COMMENT 'Product ID',
          name VARCHAR(100) NOT NULL,
          description TEXT DEFAULT '' COMMENT 'description of product',
          price DECIMAL(10, 2) UNSIGNED DEFAULT '0.00' COMMENT 'Product Price',
          tax_rate DECIMAL(5, 3) COMMENT 'Applicable tax rate',
          start_time TIME(3) COMMENT 'Event start time with milliseconds',
          end_datetime DATETIME(6) DEFAULT '2023-10-22 14:30:00.123456',
          user_name VARCHAR(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
          UNIQUE INDEX idx_product_name (name),
          INDEX idx_price (product_id,price)
      ) ENGINE=InnoDB AUTO_INCREMENT=1343 DEFAULT CHARSET=utf8mb4
      ;
      """

    val tokens = MySqlLexer.tokenize(createTableSQL)
    val parser = new MySqlParser(tokens)
    val ast = parser.parse()
    logger.info(ast.toString)
  }
}

package com.agilogy.dbmodel

import org.scalatest.{BeforeAndAfterAll, FunSpec}

class ColumnSpec extends FunSpec with DatabaseSchemaHelpers with BeforeAndAfterAll {
  override def afterAll {
    connection.close()
  }

  describe("A column") {
    val addressComment = "The user address"
    resetDb(
    s"""
       |create table ${SCHEMA_NAME}.users(
       | username text not null,
       | address varchar(10)
       |);
       |
       |comment on column ${SCHEMA_NAME}.users.address is '${addressComment}';
     """.stripMargin
    )
    val model = ModelReader.fromConnection(connection, SCHEMA_NAME)

    it("can be nullable") {
      assert(model("users").get("address").get.nullable)
    }

    it("can be not null") {
      assert(!model("users").get("username").get.nullable)
    }

    it("must have a sql type") {
      assert(model("users").get("username").get.sqlType == "text")
    }

    it("must have a java type") {
      assert(model("users").get("username").get.javaType == java.sql.Types.VARCHAR)
    }

    it("can have a max size") {
      assert(model("users").get("address").get.maxSize.get == 10)
    }

    it("can have a comment") {
      assert(model("users").get("address").get.comment == Some(addressComment))
      assert(model("users").get("username").get.comment == None)
    }
  }
}

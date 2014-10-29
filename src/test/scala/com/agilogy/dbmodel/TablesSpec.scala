package com.agilogy.dbmodel

import java.sql.DriverManager

import org.scalatest.{FunSpec, BeforeAndAfterAll, FlatSpec}

class TablesSpec extends FunSpec with DatabaseSchemaHelpers with BeforeAndAfterAll {


  override def afterAll {
    connection.close()
  }

  describe("A table") {
    resetDb(
      s"""
        |create table ${SCHEMA_NAME}.users(
        | username varchar(10),
        | primary key(username)
        |);
        |
        |create table ${SCHEMA_NAME}.addresses(
        | address varchar(10),
        | user_name varchar(10),
        | foreign key(user_name) references ${SCHEMA_NAME}.users(username)
        |);
        |
        |create index addrIdx on ${SCHEMA_NAME}.addresses(address);
      """.stripMargin)
    val model = ModelReader.fromConnection(connection, SCHEMA_NAME)

    it("should have a name") {
      assert(model.tables.map(_.name) == Set("users", "addresses"))
    }

    it("should have columns") {
      assert(model("users").get.columns.size == 1)

    }
    it("should have an optional primary key") {
      assert(model("users").get.primaryKey.get == "username")
      assert(model("addresses").get.primaryKey.isEmpty)
    }

    it("should have foreign keys") {
      assert(model("addresses").get.foreignKeys.size == 1)
    }

    it("should have indices") {
      assert(model("addresses").get.indices.contains(Index(
        name="addridx",
        table="addresses",
        column="address",
        unique=false
      )))

    }

  }

}

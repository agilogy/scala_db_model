package com.agilogy.dbmodel

import org.scalatest.{BeforeAndAfterAll, FunSpec}

class ForeignKeySpec extends FunSpec with DatabaseSchemaHelpers with BeforeAndAfterAll {
  override def afterAll {
    connection.close()
  }

  describe("A foreign key made of one single column") {
    resetDb(
      s"""
       |create table ${SCHEMA_NAME}.users(
       | username text not null,
       | primary key(username)
       |);
       |
       |create table ${SCHEMA_NAME}.cities(
       |  name text not null,
       |  primary key(name)
       |);
       |
       |create table ${SCHEMA_NAME}.addresses(
       |  username text,
       |  address text,
       |  city text not null,
       |  foreign key(username) references users(username)
       |);
       |
       |alter table ${SCHEMA_NAME}.addresses add constraint addr_city_fk foreign key(city) references ${SCHEMA_NAME}.cities(name)
     """.stripMargin
    )
    val model = ModelReader.fromConnection(connection, SCHEMA_NAME)

    it("should be in the model") {
      assert(model("addresses").get.fk("username").isDefined)
      assert(model("addresses").get.fk("city").isDefined)
    }
    val users_fk = model("addresses").get.fk("username").get
    val addr_city_fk = model("addresses").get.fk("city").get

    it("points to the target table") {
      assert(users_fk.referencedTable == "users")
      assert(users_fk.columns(0).references == "username")

      assert(addr_city_fk.columns(0).name == "city")
      assert(addr_city_fk.referencedTable == "cities")
      assert(addr_city_fk.columns(0).references == "name")
    }

    it("can have a name") {
      assert(addr_city_fk.name == "addr_city_fk")
    }
  }

  describe("A foreign key made of multiple columns") {
    resetDb(
      s"""
       |create table ${SCHEMA_NAME}.cities(
       |  name text not null,
       |  country text not null,
       |  primary key(name, country)
       |);
       |
       |create table ${SCHEMA_NAME}.users(
       | username text not null,
       | city_name text not null,
       | city_country text not null,
       | foreign key (city_name, city_country) references ${SCHEMA_NAME}.cities(name, country)
       |);
     """.stripMargin
    )
    val model = ModelReader.fromConnection(connection, SCHEMA_NAME)
    val fk = model("users").get.foreignKeys.head
    it ("should have multiple column names") {
      assert(fk.referencedTable == "cities")
      assert(fk.columns(0).name == "city_name")
      assert(fk.columns(0).references == "name")
      assert(fk.columns(1).name == "city_country")
      assert(fk.columns(1).references == "country")

    }
  }

}
package com.agilogy.dbmodel

import java.sql.DriverManager

trait DatabaseSchemaHelpers {
  DriverManager.registerDriver(new org.postgresql.Driver())
  val connection = DriverManager.getConnection("jdbc:postgresql:test", System.getProperty("user.name"), null);

  val SCHEMA_NAME = "test"

  protected def executeSql(sql: String): Unit = {
    val s = connection.createStatement()
    try {
    s.execute(sql)
    } catch {
      case e:Exception=> {
        println("Error executing statement: \n" + sql)
        e.printStackTrace()
        throw e
      }
    }
    s.close()
  }

  def resetDb(ddl: String) {
    val metadata = connection.getMetaData
    val schemas = metadata.getSchemas(null, SCHEMA_NAME)
    if (schemas.next()) {
      executeSql(s"DROP SCHEMA $SCHEMA_NAME CASCADE")
    }
    executeSql(s"CREATE SCHEMA $SCHEMA_NAME")
    val statements = ddl.split("\\n\\n")
    statements.foreach(s => {
      val sql =
        if (s.trim().endsWith(";")) {
          s.trim().substring(0, s.trim().length - 1)
        } else {
          s.trim()
        }
      executeSql(sql)
    })
  }
}

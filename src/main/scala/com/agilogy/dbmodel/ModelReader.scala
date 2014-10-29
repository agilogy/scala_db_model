package com.agilogy.dbmodel

import java.sql.{DatabaseMetaData, Connection}

import scala.collection.mutable.ListBuffer

object ModelReader {

  def fromConnection(c: Connection, schemaName: String): Model = {
    val metadata = c.getMetaData
    val tables = findTables(metadata, schemaName)
    Model(tables = tables)
  }

  private def findTables(metadata: DatabaseMetaData, schemaName: String): Set[Table] = {

    val tablesRs = metadata.getTables(null, null, null, Array("TABLE"));
    val tables = new ListBuffer[Table]
    while (tablesRs.next()) {
      val tableName = tablesRs.getString("TABLE_NAME")
      val cols = findColumns(metadata, schemaName, tableName)
      val pk = findPrimaryKey(metadata, schemaName, tableName)
      val fks = findForeignKeys(metadata, schemaName, tableName)
      val idxs = findIndices(metadata, schemaName, tableName)

      tables.append(Table(
        name = tableName,
        columns = cols,
        primaryKey = pk,
        foreignKeys = fks,
        indices = idxs))
    }
    tables.toSet
  }

  private def findColumns(md: DatabaseMetaData, schemaName: String, tableName: String): Seq[Column] = {
    val rs = md.getColumns(null, schemaName, tableName, "%")
    val columns = new ListBuffer[Column]
    while (rs.next()) {
      val notNull = rs.getInt("NULLABLE") == DatabaseMetaData.columnNoNulls
      columns.append(Column(
        name = rs.getString("COLUMN_NAME"),
        nullable = !notNull,
        sqlType = rs.getString("TYPE_NAME") ,
        javaType = rs.getInt("DATA_TYPE"),
        maxSize = Option(rs.getInt("COLUMN_SIZE")),
        comment = Option(rs.getString("REMARKS"))
      ))
    }
    return columns.toSeq
  }

  private def findPrimaryKey(md: DatabaseMetaData, schemaName: String, tableName: String): Option[PrimaryKey] = {
    val rs = md.getPrimaryKeys(null, schemaName, tableName)

    if (rs.next()) {
      Some(PrimaryKey(rs.getString("COLUMN_NAME")))
    } else {
      None
    }
  }

  private def findForeignKeys(md: DatabaseMetaData, schemaName: String, tableName: String): Set[ForeignKey] = {
    val rs = md.getImportedKeys(null, schemaName, tableName)
    val fks = new ListBuffer[ForeignKey]

    while (rs.next()) {
      val columnName = rs.getString("FKCOLUMN_NAME")
      val pkColumnName = rs.getString("PKCOLUMN_NAME")
      fks.append(ForeignKey(
        columns = Seq(ForeignKeyColumn(columnName, pkColumnName)),
        referencedTable = rs.getString("PKTABLE_NAME"),
        name = rs.getString("FK_NAME")
      ))
    }
    return fks.groupBy(_.name).map {
      case (name:String, columns: ListBuffer[ForeignKey]) =>
        ForeignKey(
          name = name,
          referencedTable = columns.head.referencedTable,
          columns = columns.map(_.columns.head)
        )
    }.toSet
  }

  private def findIndices(md: DatabaseMetaData, schemaName:String, tableName:String): Set[Index] = {
    val rs = md.getIndexInfo(null, null, tableName, false, false )
    val idxs = new ListBuffer[Index]

    while(rs.next()) {
      idxs.append(Index(
        name = rs.getString("INDEX_NAME"),
        table = rs.getString("TABLE_NAME"),
        column = rs.getString("COLUMN_NAME"),
        unique = ! rs.getBoolean("NON_UNIQUE")
      ))
    }
    idxs.toSet
  }
}

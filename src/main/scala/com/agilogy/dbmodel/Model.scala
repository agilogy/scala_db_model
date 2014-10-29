package com.agilogy.dbmodel

//Inspired by https://github.com/slick/slick/blob/master/src/main/scala/scala/slick/model/Model.scala
case class Model(tables: Set[Table]) {
  def apply(tableName:String): Option[Table] = tables.find(_.name == tableName)
}

//TODO: Table FQN (catalog, schema, table)
case class Table(
                  name:String,
                  columns: Seq[Column],
                  primaryKey: Option[PrimaryKey] = None,
                  foreignKeys: Set[ForeignKey] = Set.empty,
                  indices: Set[Index] = Set.empty) {
  def apply(columnName:String):Option[Column] = columns.find(_.name==columnName)

  def fk(name:String): Option[ForeignKey] = {
    foreignKeys.find(_.columns.exists(c=>c.name == name))
  }
}

case class PrimaryKey(name:String) extends AnyVal

case class Column(
                   name: String,
                   nullable: Boolean,
                   sqlType: String,
                   javaType: Int,
                   maxSize: Option[Int],
                   comment: Option[String])

case class ForeignKey(
                       referencedTable: String,
                       columns: Seq[ForeignKeyColumn],
                       name:String) {
  require(name != null, "A Foreign key must have a name " + this)
}


case class ForeignKeyColumn(name:String, references:String)

case class Index(
                  name: String,
                  table: String,
                  column: String,
                  unique: Boolean
                  )
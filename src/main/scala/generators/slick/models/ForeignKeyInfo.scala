package generators.slick.models

import scala.slick.model.{QualifiedName, ForeignKey, Table, Model}

class ForeignKeyInfo(model : Model) {

  val tables = model.tables

  val tablesByName = model.tablesByName

  val foreignKeys : Seq[ForeignKey] = {
    (tables map {_.foreignKeys}).flatten
  }

  val tablesForeignKeys : Map[QualifiedName, Seq[ForeignKey]] = (tables map {table =>
    (table.name, table.foreignKeys)
  }).toMap

  val foreignKeysReferencedTables : Map[QualifiedName, Seq[ForeignKey]]= (tables map {table =>
    (table.name, foreignKeys.filter(_.referencedTable == table.name))
  }).toMap

  def foreignKeysReferencedTable(name : QualifiedName): Seq[ForeignKey] = foreignKeysReferencedTables(name)

}


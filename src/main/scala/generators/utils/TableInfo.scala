package generators.utils

import scala.slick.model.{Column, ForeignKey, Table}

class TableInfo(val table : Table) extends GeneratorHelpers{

  val columns : Seq[Column] = table.columns

  val foreignKeys : Seq[ForeignKey] = table.foreignKeys

  val name : String = table.name.table

  val nameCamelCased : String = name.toCamelCase

  val nameCamelCasedUncapitalized : String = nameCamelCased.uncapitalize

  val listName : String = nameCamelCasedUncapitalized + "s"

  val daoObjectName : String = nameCamelCased + "Dao"

  val tableRowName : String = nameCamelCased + "Row"

  val queryObjectName : String = nameCamelCased

  lazy val primaryKeyOpt = columns.find(_.options.contains(scala.slick.ast.ColumnOption.PrimaryKey))

  lazy val (primaryKeyName, primaryKeyType) = primaryKeyOpt match {
        case Some(col) => (standardColumnName(col.name), col.tpe)
        case None => {
          val col = columns.head
          (standardColumnName(col.name), col.tpe)
        }
      }

  val formName = nameCamelCasedUncapitalized + "Form"

  val controllerName = nameCamelCased + "Controller"

  val viewsPackage = name.toLowerCase

  val isJunctionTable = !columns.exists(_.options.contains(scala.slick.ast.ColumnOption.PrimaryKey)) && foreignKeys.length >= 2

}

package generators.slick.utils

import scala.slick.model.{ForeignKey, Column, Table}
import generators.utils.StringUtils
import scala.slick.ast.ColumnOption.PrimaryKey

class TableInfo(val table : Table) extends StringUtils{

  val columns : Seq[Column] = table.columns

  val foreignKeys : Seq[ForeignKey] = table.foreignKeys

  val name : String = table.name.table

  val listName : String = name + "s"

  val nameCamelCased : String = name.toCamelCase

  val nameCamelCasedUncapitalized : String = nameCamelCased.uncapitalize

  val daoObjectName : String = nameCamelCased + "Dao"

  val tableRowName : String = nameCamelCased + "Row"

  val queryObjectName : String = nameCamelCased

  lazy val primaryKeyOpt = columns.find(_.options.contains(PrimaryKey))

  lazy val (primaryKeyName, primaryKeyType) = primaryKeyOpt match {
        case Some(col) => (col.name, col.tpe)
        case None => {
          val col = columns.head
          (col.name, col.tpe)
        }
      }

  val formName = nameCamelCasedUncapitalized + "Form"

  val controllerName = nameCamelCased + "Controller"

  val viewsPackage = name.toLowerCase

  val isJunctionTable = !columns.exists(_.options.contains(PrimaryKey)) && foreignKeys.length >= 2

}

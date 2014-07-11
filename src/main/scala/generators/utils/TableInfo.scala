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

  val primaryKeyOpt = columns.find(_.options.contains(scala.slick.ast.ColumnOption.PrimaryKey))

  val (primaryKeyName, primaryKeyType) = primaryKeyOpt match {
        case Some(col) => (standardColumnName(col.name), col.tpe)
        case None => {
          val col = columns.head
          (standardColumnName(col.name), col.tpe)
        }
      }

  val primaryKeyColumns : Seq[Column]= {
    table.primaryKey match {
      case Some(pk) => pk.columns
      case None => primaryKeyOpt match {
        case Some(col) => Seq(col)
        case None => Seq(columns.head)
      }
    }
  }

  val formName = nameCamelCasedUncapitalized + "Form"

  val controllerName = nameCamelCased + "Controller"

  val viewsPackage = name.toLowerCase

  val tableConfig = TablesConfigParser.getTableConfigForName(name)

  lazy val listColumns : Seq[Column] = getListColumns

  private def getListColumns : Seq[Column] = {

    val listCols : Option[Seq[Column]] = tableConfig.map( cfg => cfg.listColumns.map(mapColumnNamesToColumns(_))).flatten

    listCols match {
      case Some(cols) if cols.nonEmpty => cols
      case Some(cols) if cols.isEmpty => columns.take(5)
      case None => columns.take(5)
    }
  }

  lazy val selectColumns : Seq[Column] = getSelectColumns

  private def getSelectColumns : Seq[Column] = {

    val selectCols : Option[Seq[Column]] = tableConfig.map( cfg => cfg.selectColumns.map(mapColumnNamesToColumns(_))).flatten

    selectCols match {
      case Some(cols) if cols.nonEmpty => cols
      case Some(cols) if cols.isEmpty => columns.take(5)
      case None => columns.take(5)
    }
  }

  private def mapColumnNamesToColumns(names : List[String]) : Seq[Column] = {
    val filteredColumns = names.map{ colName =>
      columns.find(_.name == colName)
    }.filter(_.isDefined).map(_.get)

    filteredColumns
  }

  lazy val isJunctionTable = getIsJunctionTable

  private def getIsJunctionTable : Boolean = {
    val isJunctionFromConfig : Option[Boolean] = tableConfig.map( cfg => cfg.isJunctionTable.map(value => value)).flatten

    isJunctionFromConfig match {
      case Some(isJunction) => isJunction
      case None => false
    }
  }

  val isSimpleJunctionTable = defaultIsJunctionTableCheck

  private def defaultIsJunctionTableCheck : Boolean = table.primaryKey.isEmpty && !columns.exists(_.options.contains(scala.slick.ast.ColumnOption.PrimaryKey)) && foreignKeys.length == 2

}

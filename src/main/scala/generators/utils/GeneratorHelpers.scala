package generators.utils

import scala.slick.ast.ColumnOption.AutoInc
import scala.slick.model.Column

trait GeneratorHelpers extends StringUtils {

  def importCode(importPath : String) = "import " + importPath;

  def standardColumnName(name : String) = name.toLowerCase.toCamelCase.uncapitalize

  def isAutoIncColumn(column : Column) : Boolean = column.options.contains(AutoInc)

  def makeArgsWithTypes(columns : Seq[Column]) : String = columns.map {column =>
    standardColumnName(column.name) + " : " + column.tpe
  }.mkString(", ")

  def makeArgsWithoutTypes(columns : Seq[Column]) : String = columns.map {column =>
    standardColumnName(column.name)
  }.mkString(", ")

  def makeColumnsAndString(columns : Seq[Column]) : String = columns.map(col => standardColumnName(col.name).capitalize).mkString("And")
}
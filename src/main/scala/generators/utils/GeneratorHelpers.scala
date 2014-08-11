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

  def makeArgsWithColumnTypes(columns : Seq[Column]) : String = columns.map {column =>
    standardColumnName(column.name) + " : Column[" + column.tpe + "]"
  }.mkString(", ")

  def makeArgsWithoutTypes(columns : Seq[Column]) : String = columns.map {column =>
    standardColumnName(column.name)
  }.mkString(", ")

  def makeArgsTypes(columns : Seq[Column]) : String = columns.map {column =>
    column.tpe
  }.mkString(", ")

  def makeArgsWithObjectWithoutTypes(objectName : String, columns : Seq[Column]) : String = columns.map {column =>
    objectName + "." + standardColumnName(column.name)
  }.mkString(", ")

  def makeSlickRowComparing(columns : Seq[Column]) : String = columns.map { column =>
    val colName = standardColumnName(column.name)
    s"""row.${colName} === ${colName}"""
  }.mkString(" && ")
  
  def makeSquerylRowComparing(columns : Seq[Column]) : String = columns.map { column =>
    val colName = standardColumnName(column.name)
    if(column.tpe.equals("String") && column.nullable){
      s"""row.${colName} === Some(${colName})"""
    }
    else {
      s"""row.${colName} === ${colName}"""
    }
  }.mkString(" and ")

  def makeColumnsAndString(columns : Seq[Column]) : String = columns.map(col => standardColumnName(col.name).capitalize).mkString("And")

  def makeFindByMethodName(columns : Seq[Column]) : String = {
    "findBy" + makeColumnsAndString(columns)
  }

  def makeDeleteByMethodName(columns : Seq[Column]) : String = {
    "deleteBy" + makeColumnsAndString(columns)
  }

  def makeShowByMethodName(columns : Seq[Column]) : String = {
    "showBy" + makeColumnsAndString(columns)
  }

  def makeFindByQueryMethodName(columns : Seq[Column]) : String = {
    makeFindByMethodName(columns) + "Query"
  }

  def makeFindByQueryCompiledMethodName(columns : Seq[Column]) : String = {
    makeFindByQueryMethodName(columns) + "Compiled"
  }

  def makeCompositeKeyType(columns : Seq[Column]) : String = {
    if(columns.length == 1) {
      makeArgsTypes(columns)
    } else {
      s"""CompositeKey${columns.length}[${makeArgsTypes(columns)}]"""
    }
  }

  def makeCompositeKey(columns : Seq[Column]) : String = {
    if(columns.length == 1) {
      standardColumnName(columns.head.name)
    } else {
      s"""compositeKey(${makeArgsWithoutTypes(columns)})"""
    }
  }

  def makeArrowAssoc(columns : Seq[Column]) : String = columns.map { column =>
    s"""'${standardColumnName(column.name)} -> ${standardColumnName(column.name)}"""
  }.mkString(",\n\t\t\t\t")

  def makeArrowAssocWithObjectName(objectName : String, columns : Seq[Column]) : String = columns.map { column =>
    s"""'${standardColumnName(column.name)} -> ${objectName}.${standardColumnName(column.name)}"""
  }.mkString(",\n\t\t\t\t")

  def makeSQLColumnAssigning(column : Column) : String = {
    s"""\"${column.name}\" = {${standardColumnName(column.name)}}"""
  }

  def makeColumnNamesList(columns : Seq[Column]) : String = columns.map { column =>
    "\"" + column.name + "\""
  }.mkString(", ")

  def makeValuesList(columns : Seq[Column]) : String = columns.map { column =>
    "{" + standardColumnName(column.name) + "}"
  }.mkString(", ")
}
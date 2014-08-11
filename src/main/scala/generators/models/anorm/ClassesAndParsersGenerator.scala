package generators.models.anorm

import generators.utils._

import scala.slick.model.{Column, Model}

object ClassesAndParsersGenerator {
  def generate(outputFolder : String) = {

    val appConfig = AppConfigParser.getAppConfig

    val pkg = appConfig.modelsPackage

    val model = new ModelProvider(appConfig).model

    new ClassesAndParsersGenerator(model).writeToFile(outputFolder, pkg)
  }
}

class ClassesAndParsersGenerator(model : Model) extends OutputHelpers with GeneratorHelpers {

  val objectName = "Tables"

  val allTablesInfo = model.tables.map(new TableInfo(_))

  override def code: String = objectCode

  override def indent(code: String): String = code

  def objectCode : String = {
    s"""

object ${objectName} {

  ${imports}

  ${classes}

  ${parsers}
}
""".trim()
  }

  def imports : String = {
    Seq(importCode("anorm._"),
        importCode("anorm.SqlParser._"),
        importCode("utils.CustomColumns._"))
        .mkString("\n\t")
  }

  def classes = {
    allTablesInfo.map(printCaseClass(_)).mkString("\n\n\t")
  }

  def printCaseClass(tableInfo : TableInfo) = {
    s"""
  case class ${tableInfo.tableRowName}(
    ${printFields(tableInfo)})""".trim
  }

  def printFields(tableInfo : TableInfo) : String = {
    tableInfo.columns.map(printField(_)).mkString(", \n\t\t")
  }

  def printField(column : Column) = {
    if(column.nullable) s"""${standardColumnName(column.name)} : Option[${column.tpe}]"""
    else s"""${standardColumnName(column.name)} : ${column.tpe}"""
  }

  def parsers = {
    allTablesInfo.map(printParser(_)).mkString("\n\n\t")
  }

  def printParser(tableInfo : TableInfo) = {

    val singleParsers = tableInfo.columns.map(printSingleParser(_)).mkString(" ~\n\t\t")

    s"""
  val ${tableInfo.parserName} = {
    ${singleParsers} map {
      ${printCase(tableInfo.columns)} =>
        ${printRowMapping(tableInfo)}
    }
  }
     """.trim
  }

  def printSingleParser(column : Column) = {
    if(column.nullable){
      s"""get[Option[${column.tpe}]]("${column.name}")"""
    }
    else {
      s"""get[${column.tpe}]("${column.name}")"""
    }
  }

  def printCase(columns : Seq[Column]) : String = {
    "case " + columns.map(_.name).mkString("~")
  }

  def printRowMapping(tableInfo : TableInfo) : String = {
    tableInfo.tableRowName + "(" + tableInfo.columns.map(_.name).mkString(", ") + ")"
  }


  override def writeToFile(folder:String, pkg: String, fileName: String= "Tables.scala") {
      writeStringToFile(packageCode(pkg), folder, pkg, fileName)
    }

}

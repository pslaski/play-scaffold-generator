package org.modelgenerator

import scala.reflect.runtime._
import scala.slick.model.Table
import scala.slick.ast.ColumnOption.PrimaryKey

object DaoObjectGenerator {
  def generate(args: Array[String]) = {

    val jdbcDriver = args(0)
    val url = args(1)
    val outputFolder = args(2)
    val pkg = args(3)
    val user = Option(args(4)) getOrElse("")
    val password = Option(args(5)) getOrElse("")
    
    val slickDriver = DriverLoader.slickDriver(jdbcDriver)

    val db = slickDriver.simple.Database.forURL(url,driver=jdbcDriver, user = user, password = password)
    val model = db.withSession(slickDriver.createModel(_))

    model.tables map { table =>
      new DaoObjectGenerator(table).writeToFile(outputFolder, pkg)
    }
  }
}

class DaoObjectGenerator(table : Table) extends OutputHelpers with GeneratorHelpers {

  val objectName = table.name.table.toCamelCase + "Dao"

  val tableRowName = table.name.table.toCamelCase + "Row"

  val primaryKeyOpt = table.columns.find(_.options.contains(PrimaryKey))

  val queryObjectName = table.name.table.toCamelCase

  override def code: String = objectCode(objectName)

  override def indent(code: String): String = code

  def objectCode(objectName : String) : String = {
    s"""
object ${objectName} {

${imports}

${methods}

}""".trim()
  }

  def imports : String = {
    Seq(importCode("utils.DbConnection._"),
        importCode("Tables._"),
        importCode("Tables.profile.simple._"))
        .mkString("\n\n")
  }

  def methods : String = {
      primaryKeyOpt match {
        case Some(col) => {
          val (primaryKeyName, primaryKeyType) = (col.name, col.tpe)

          Seq(saveMethodCode(tableRowName, primaryKeyName, primaryKeyType, queryObjectName),
                  findByIdMethodCode(tableRowName, primaryKeyName, primaryKeyType, queryObjectName),
                  updateMethodCode(tableRowName, primaryKeyName, queryObjectName),
                  deleteMethodCode(primaryKeyName, primaryKeyType, queryObjectName),
                  findAllMethodCode(tableRowName, queryObjectName))
                  .mkString("\n\n")
        }
        case None => Seq(findAllMethodCode(tableRowName, queryObjectName))
                .mkString("\n\n")
      }
  }

  override def writeToFile(folder:String, pkg: String, fileName: String= objectName +  ".scala") {
      writeStringToFile(packageCode(pkg), folder, pkg, fileName)
    }
}

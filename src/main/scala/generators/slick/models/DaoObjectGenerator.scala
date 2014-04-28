package generators.slick.models

import scala.slick.model.Table
import generators.slick.utils.{DriverLoader, SlickGeneratorHelpers}
import generators.utils.{Config, OutputHelpers}

object DaoObjectGenerator {
  def generate(config : Config, outputFolder : String) = {

    val jdbcDriver = config.jdbcDriver
    val url = config.url
    val pkg = config.modelsPackage
    val user = config.user
    val password = config.password
    
    val slickDriver = DriverLoader.slickDriver(jdbcDriver)

    val db = slickDriver.simple.Database.forURL(url,driver=jdbcDriver, user = user, password = password)
    val model = db.withSession(slickDriver.createModel(_))

    model.tables map { table =>
      new DaoObjectGenerator(table).writeToFile(outputFolder, pkg)
    }
  }
}

class DaoObjectGenerator(table : Table) extends OutputHelpers with DaoGeneratorHelpers with SlickGeneratorHelpers {

  override val columns = table.columns

  val objectName = table.name.table.toCamelCase + "Dao"

  val tableRowName = table.name.table.toCamelCase + "Row"

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
        .mkString("\n")
  }

  def methods : String = {
    Seq(saveMethodCode(tableRowName, primaryKeyName, primaryKeyType, queryObjectName),
              findByIdMethodCode(tableRowName, primaryKeyName, primaryKeyType, queryObjectName),
              updateMethodCode(tableRowName, primaryKeyName, queryObjectName),
              deleteMethodCode(primaryKeyName, primaryKeyType, queryObjectName),
              findAllMethodCode(tableRowName, queryObjectName))
              .mkString("\n\n")
  }

  override def writeToFile(folder:String, pkg: String, fileName: String= objectName +  ".scala") {
      writeStringToFile(packageCode(pkg), folder, pkg, fileName)
    }
}

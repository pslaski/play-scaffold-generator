package generators.slick.models

import scala.slick.model.Table
import generators.slick.utils.{TableInfo, SlickGeneratorHelpers}
import generators.utils.{ModelProvider, Config, OutputHelpers}

object DaoObjectGenerator {
  def generate(config : Config, outputFolder : String) = {

    val pkg = config.modelsPackage

    val model = new ModelProvider(config).model

    model.tables map { table =>
      new DaoObjectGenerator(table).writeToFile(outputFolder, pkg)
    }
  }
}

class DaoObjectGenerator(table : Table) extends OutputHelpers with DaoGeneratorHelpers with SlickGeneratorHelpers {

  val tableInfo = new TableInfo(table)

  override val rowName: String = tableInfo.name.toLowerCase

  override val primaryKeyName: String = tableInfo.primaryKeyName
  override val primaryKeyType: String = tableInfo.primaryKeyType

  override val tableRowName: String = tableInfo.tableRowName
  override val queryObjectName: String = tableInfo.queryObjectName

  val objectName = tableInfo.daoObjectName

  override def code: String = objectCode

  override def indent(code: String): String = code

  def objectCode : String = {
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
    Seq(saveMethodCode,
        findByIdMethodCode,
        updateMethodCode,
        deleteMethodCode,
        findAllMethodCode)
        .mkString("\n\n")
  }

  override def writeToFile(folder:String, pkg: String, fileName: String= objectName +  ".scala") {
      writeStringToFile(packageCode(pkg), folder, pkg, fileName)
    }

}

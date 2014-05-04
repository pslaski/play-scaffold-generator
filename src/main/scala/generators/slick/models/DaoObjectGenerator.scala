package generators.slick.models

import scala.slick.model.{ForeignKeyAction, Table}
import generators.slick.utils.{ForeignKeyInfo, TableInfo, SlickGeneratorHelpers}
import generators.utils.{ModelProvider, Config, OutputHelpers}

object DaoObjectGenerator {
  def generate(config : Config, outputFolder : String) = {

    val pkg = config.modelsPackage

    val model = new ModelProvider(config).model

    val foreignKeyInfo = new ForeignKeyInfo(model)

    model.tables map { table =>
      new DaoObjectGenerator(table, foreignKeyInfo).writeToFile(outputFolder, pkg)
    }
  }
}

class DaoObjectGenerator(table : Table, foreignKeyInfo : ForeignKeyInfo) extends OutputHelpers with DaoGeneratorHelpers with SlickGeneratorHelpers {

  val tableInfo = new TableInfo(table)

  override val rowName: String = tableInfo.name.toLowerCase

  override val rowNameCamelCased : String = rowName.toCamelCase

  override val primaryKeyName: String = tableInfo.primaryKeyName
  override val primaryKeyType: String = tableInfo.primaryKeyType

  override val tableRowName: String = tableInfo.tableRowName
  override val queryObjectName: String = tableInfo.queryObjectName

  val objectName = tableInfo.daoObjectName

  override val fieldsForSimpleName = tableInfo.columns.take(5).map(_.name)

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
    (fixedMethods ++ dynamicMethods).mkString("\n\n")
  }

  def fixedMethods : Seq[String] = {

    val cascadeChildData = foreignKeyInfo.foreignKeysReferencedTable(table.name).map{ fk =>
      fk.onDelete match {
        case ForeignKeyAction.Restrict => {
          val tab = new TableInfo(foreignKeyInfo.tablesByName(fk.referencingTable))
          (tab.daoObjectName, tab.nameCamelCased)
        }
        case _ => ("", "")
      }
    }.filter(_._1 != "")

    Seq(saveMethodCode,
        findByIdMethodCode,
        updateMethodCode,
        deleteMethodCode(cascadeChildData),
        findAllMethodCode,
        formOptionsMethodCode)
  }

  def dynamicMethods : Seq[String] = {

    table.foreignKeys.map { fk =>
      val referencedTable = new TableInfo(foreignKeyInfo.tablesByName(fk.referencedTable))
      Seq(findByForeignKeyMethodCode(fk.referencingColumns.head.name, referencedTable.nameCamelCased),
      deleteByForeignKeyMethodCode(fk.referencingColumns.head.name, referencedTable.nameCamelCased))
    }.flatten

  }

  override def writeToFile(folder:String, pkg: String, fileName: String= objectName +  ".scala") {
      writeStringToFile(packageCode(pkg), folder, pkg, fileName)
    }

}

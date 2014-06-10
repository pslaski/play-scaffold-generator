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

  val mainTableInfo = new TableInfo(table)

  override val rowName: String = mainTableInfo.name.toLowerCase

  override val rowNameCamelCased : String = rowName.toCamelCase

  override val primaryKeyName: String = mainTableInfo.primaryKeyName
  override val primaryKeyType: String = mainTableInfo.primaryKeyType

  override val tableRowName: String = mainTableInfo.tableRowName
  override val queryObjectName: String = mainTableInfo.queryObjectName

  val objectName = mainTableInfo.daoObjectName

  override val fieldsForSimpleName = mainTableInfo.columns.take(5).map{ col =>
    if(col.nullable) col.name + ".getOrElse(\"\")"
    else col.name
  }

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
    if(mainTableInfo.isJunctionTable) fixedMethodsForJunctionTable
    else fixedMethodsForSimpleTable
  }

  def fixedMethodsForSimpleTable : Seq[String] = {
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

  def fixedMethodsForJunctionTable: Seq[String] = {

    Seq(saveJunctionMethodCode,
        deleteJunctionMethodCode(table.foreignKeys))
  }

  def dynamicMethods : Seq[String] = {

    val tableChildrenInfo = foreignKeyInfo.parentChildrenTablesInfo(table.name)

    val joinedByJunctionTable = tableChildrenInfo.filter(_.isJunctionTable).map{ childTableInfo =>
      val foreignKeyToFirstSide = childTableInfo.foreignKeys.filter(_.referencedTable == table.name).head
      val foreignKeyToSecondSide = childTableInfo.foreignKeys.filter(_.referencedTable != table.name).head
      val tableSecondSide = foreignKeyInfo.tablesByName(foreignKeyToSecondSide.referencedTable)
      val tableSecondSideInfo = new TableInfo(tableSecondSide)

      findByJunctionTableMethodCode(childTableInfo, tableSecondSideInfo, foreignKeyToFirstSide)
    }

    val joinedByForeignKey = table.foreignKeys.map { fk =>
      val referencedTableInfo = new TableInfo(foreignKeyInfo.tablesByName(fk.referencedTable))
      Seq(findByForeignKeyQueryMethodCode(referencedTableInfo, fk),
        findByForeignKeyMethodCode(referencedTableInfo),
        deleteByForeignKeyMethodCode(referencedTableInfo))
    }.flatten

    joinedByJunctionTable ++ joinedByForeignKey
  }

  override def writeToFile(folder:String, pkg: String, fileName: String= objectName +  ".scala") {
      writeStringToFile(packageCode(pkg), folder, pkg, fileName)
    }

}

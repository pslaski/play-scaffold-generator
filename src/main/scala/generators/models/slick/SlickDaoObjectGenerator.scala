package generators.models.slick

import generators.utils._

import scala.slick.model.{Column, Table}

import scala.slick.model.ForeignKeyAction._

object SlickDaoObjectGenerator {
  def generate(outputFolder : String) = {

    val appConfig = AppConfigParser.getAppConfig

    val pkg = appConfig.modelsPackage

    val model = new ModelProvider(appConfig).model

    val foreignKeyInfo = new ForeignKeyInfo(model)

    model.tables map { table =>
      new SlickDaoObjectGenerator(table, foreignKeyInfo).writeToFile(outputFolder, pkg)
    }
  }
}

class SlickDaoObjectGenerator(table : Table, foreignKeyInfo : ForeignKeyInfo) extends OutputHelpers with SlickDaoGeneratorHelpers with GeneratorHelpers {

  val mainTableInfo = new TableInfo(table)

  override val rowName: String = mainTableInfo.name.toLowerCase

  override val rowNameCamelCased : String = rowName.toCamelCase

  override val primaryKeyName: String = mainTableInfo.primaryKeyName
  override val primaryKeyType: String = mainTableInfo.primaryKeyType

  override val primaryKeyColumns: Seq[Column] = mainTableInfo.primaryKeyColumns

  override val tableRowName: String = mainTableInfo.tableRowName
  override val queryObjectName: String = mainTableInfo.queryObjectName

  val objectName = mainTableInfo.daoObjectName

  override val fieldsForSimpleName = {
    mainTableInfo.selectColumns.map{ col =>
      if(col.nullable) standardColumnName(col.name) + ".getOrElse(\"\")"
      else standardColumnName(col.name)
    }
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
    if(mainTableInfo.isSimpleJunctionTable) fixedMethodsForSimpleJunctionTable
    else fixedMethodsForTable
  }

  def fixedMethodsForTable : Seq[String] = {
    val cascadeChildData = foreignKeyInfo.foreignKeysReferencedTable(table.name).filter(_.onDelete.action == Restrict.action).map{ fk =>
      val tab = new TableInfo(foreignKeyInfo.tablesByName(fk.referencingTable))
      (tab, fk)
    }

    val saveMethod : String= {
      val autoIncCol = primaryKeyColumns.find(isAutoIncColumn(_))
      autoIncCol match {
        case Some(col) => saveReturnIdMethodCode(standardColumnName(col.name))
        case None => saveSimpleMethodCode
      }
    }

    saveMethod +: Seq(findByIdMethodCode,
                      updateMethodCode,
                      deleteMethodCode(cascadeChildData),
                      findAllMethodCode)
  }

  def fixedMethodsForSimpleJunctionTable: Seq[String] = {

    Seq(saveSimpleMethodCode,
        deleteJunctionMethodCode(table.foreignKeys))
  }

  def dynamicMethods : Seq[String] = {

    val tableChildrenInfo = foreignKeyInfo.parentChildrenTablesInfo(table.name)

    val formOptions = foreignKeyInfo.foreignKeysReferencedTable(table.name).map(_.referencedColumns.head).distinct.map(col => formOptionsMethodCode(standardColumnName(col.name)))

    val joinedByJunctionTable = tableChildrenInfo.filter(tabInfo => tabInfo.isJunctionTable || tabInfo.isSimpleJunctionTable).map{ childTableInfo =>
      val foreignKeyToFirstSide = childTableInfo.foreignKeys.filter(_.referencedTable == table.name).head
      val foreignKeyToSecondSide = childTableInfo.foreignKeys.filter(_.referencedTable != table.name).head
      val tableSecondSide = foreignKeyInfo.tablesByName(foreignKeyToSecondSide.referencedTable)
      val tableSecondSideInfo = new TableInfo(tableSecondSide)

      findByJunctionTableMethodCode(childTableInfo, tableSecondSideInfo, foreignKeyToFirstSide, foreignKeyToSecondSide)
    }

    val joinedByForeignKey = table.foreignKeys.map { fk =>
      val referencedTableInfo = new TableInfo(foreignKeyInfo.tablesByName(fk.referencedTable))
      Seq(findByForeignKeyQueryMethodCode(referencedTableInfo, fk),
        findByForeignKeyMethodCode(referencedTableInfo, fk),
        deleteByForeignKeyMethodCode(referencedTableInfo, fk))
    }.flatten

    formOptions ++ joinedByJunctionTable ++ joinedByForeignKey
  }

  override def writeToFile(folder:String, pkg: String, fileName: String= objectName +  ".scala") {
      writeStringToFile(packageCode(pkg), folder, pkg, fileName)
    }
}

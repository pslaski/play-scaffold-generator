package generators.models.squeryl

import generators.utils._

import scala.slick.model.ForeignKeyAction.{NoAction, Restrict}
import scala.slick.model.{Column, Table}

object SquerylDaoObjectGenerator {
  def generate(outputFolder : String) = {

    val appConfig = AppConfigParser.getAppConfig

    val pkg = appConfig.modelsPackage

    val model = new ModelProvider(appConfig).model

    val foreignKeyInfo = new ForeignKeyInfo(model)

    model.tables map { table =>
      new SquerylDaoObjectGenerator(table, foreignKeyInfo).writeToFile(outputFolder, pkg)
    }
  }
}

class SquerylDaoObjectGenerator(table : Table, foreignKeyInfo : ForeignKeyInfo) extends OutputHelpers with SquerylDaoGeneratorHelpers with GeneratorHelpers {

  val mainTableInfo = new TableInfo(table)

  override val rowName: String = mainTableInfo.name.toLowerCase

  override val listName : String = mainTableInfo.listName

  override val primaryKeyColumns: Seq[Column] = mainTableInfo.primaryKeyColumns

  override val tableRowName: String = mainTableInfo.tableRowName
  override val queryObjectName: String = mainTableInfo.queryObjectName

  val objectName = mainTableInfo.daoObjectName

  override val fieldsForSimpleName = mainTableInfo.selectColumns.map{ col =>
    if(col.nullable) standardColumnName(col.name) + ".getOrElse(\"\")"
    else standardColumnName(col.name)
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
    Seq(importCode("Tables._"),
        importCode("org.squeryl.PrimitiveTypeMode._"))
        .mkString("\n")
  }

  def methods : String = {
    (fixedMethods ++ dynamicMethods).mkString("\n\n")
  }

  def fixedMethods : Seq[String] = {
    if(mainTableInfo.isSimpleJunctionTable) fixedMethodsForSimpleJunctionTable
    else fixedMethodsForSimpleTable
  }

  def fixedMethodsForSimpleTable : Seq[String] = {

    val cascadeChildData = foreignKeyInfo.foreignKeysReferencedTable(table.name).filter(fk => fk.onDelete.action == Restrict.action || fk.onDelete.action == NoAction.action).map{ fk =>
      val tab = new TableInfo(foreignKeyInfo.tablesByName(fk.referencingTable))
      (tab, fk)
    }

    Seq(saveMethodCode,
        findByPrimaryKeyMethodCode,
        updateMethodCode,
        deleteMethodCode(cascadeChildData),
        findAllMethodCode)
  }

  def fixedMethodsForSimpleJunctionTable: Seq[String] = {
    Seq(saveMethodCode,
        deleteSimpleJunctionMethodCode(table.foreignKeys))
  }

  def dynamicMethods : Seq[String] = {

    val tableChildrenInfo = foreignKeyInfo.parentChildrenTablesInfo(table.name)

    val columnsReferenced = foreignKeyInfo.foreignKeysReferencedTable(table.name).map(_.referencedColumns).distinct

    val formOptions = columnsReferenced.map(cols => formOptionsMethodCode(cols.head.name))

    val uniqueFindByMethods = columnsReferenced.filterNot(_.equals(primaryKeyColumns)).map(cols =>
          Seq(findByUniqueMethodCode(cols),
              deleteByMethodCode(cols))).flatten

    val findByMethods = table.foreignKeys.map { fk =>
      val columns = fk.referencingColumns
      Seq(findByQueryMethodCode(columns),
          findByMethodCode(columns),
          deleteByMethodCode(columns))
    }.flatten

    val joinedByJunctionTableMethods = tableChildrenInfo.filter(tabInfo => tabInfo.isJunctionTable || tabInfo.isSimpleJunctionTable).map{ childTableInfo =>
      val foreignKeyToFirstSide = childTableInfo.foreignKeys.filter(_.referencedTable == table.name).head
      val foreignKeyToSecondSide = childTableInfo.foreignKeys.filter(_.referencedTable != table.name).head

      findByJunctionTableMethodsCode(childTableInfo, foreignKeyToFirstSide, foreignKeyToSecondSide)
    }

    formOptions ++ findByMethods ++ uniqueFindByMethods ++ joinedByJunctionTableMethods
  }

  override def writeToFile(folder:String, pkg: String, fileName: String= objectName +  ".scala") {
      writeStringToFile(packageCode(pkg), folder, pkg, fileName)
    }

}

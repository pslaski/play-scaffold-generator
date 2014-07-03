package generators.models.squeryl

import generators.utils._

import scala.slick.model.Table

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

  override val primaryKeyName: String = mainTableInfo.primaryKeyName
  override val primaryKeyType: String = mainTableInfo.primaryKeyType

  override val tableRowName: String = mainTableInfo.tableRowName
  override val queryObjectName: String = mainTableInfo.queryObjectName

  val objectName = mainTableInfo.daoObjectName

  override val fieldsForSimpleName = mainTableInfo.selectColumns.map{ col =>
    if(standardColumnName(col.name).equals(primaryKeyName)) "id"
    else if(col.nullable) standardColumnName(col.name) + ".getOrElse(\"\")"
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
    if(mainTableInfo.isJunctionTable) fixedMethodsForJunctionTable
    else fixedMethodsForSimpleTable
  }

  def fixedMethodsForSimpleTable : Seq[String] = {
    Seq(saveMethodCode,
        findByIdMethodCode,
        updateMethodCode,
        deleteMethodCode,
        findAllMethodCode,
        formOptionsMethodCode)
  }

  def fixedMethodsForJunctionTable: Seq[String] = {
    Seq(saveJunctionMethodCode,
        deleteJunctionMethodCode(table.foreignKeys))
  }

  def dynamicMethods : Seq[String] = {
    if(!mainTableInfo.isJunctionTable) dynamicMethodsForSimpleTable
    else Seq.empty
  }

  def dynamicMethodsForSimpleTable : Seq[String] = {

    val tableChildrenInfo = foreignKeyInfo.parentChildrenTablesInfo(table.name)

    val joinedByJunctionTable = tableChildrenInfo.filter(_.isJunctionTable).map{ childTableInfo =>
      val foreignKeyToSecondSide = childTableInfo.foreignKeys.filter(_.referencedTable != table.name).head
      val tableSecondSide = foreignKeyInfo.tablesByName(foreignKeyToSecondSide.referencedTable)
      val tableSecondSideInfo = new TableInfo(tableSecondSide)

      findByForeignKeyMethodCode(tableSecondSideInfo)
    }

    val joinedByForeignKey = table.foreignKeys.map { fk =>
      val referencedTableInfo = new TableInfo(foreignKeyInfo.tablesByName(fk.referencedTable))
        findByForeignKeyMethodCode(referencedTableInfo)
    }

    joinedByJunctionTable ++ joinedByForeignKey
  }

  override def writeToFile(folder:String, pkg: String, fileName: String= objectName +  ".scala") {
      writeStringToFile(packageCode(pkg), folder, pkg, fileName)
    }

}

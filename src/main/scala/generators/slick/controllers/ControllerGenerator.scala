package generators.slick.controllers

import scala.slick.model.Column
import scala.slick.model.Table
import generators.slick.utils.{ForeignKeyInfo, TableInfo}
import generators.utils.{GeneratorHelpers, ModelProvider, Config, OutputHelpers}

object ControllerGenerator {
  def generate(config : Config, outputFolder : String) = {

    val modelsPackage = config.modelsPackage
    val controllersPackage = config.controllersPackage


    val model = new ModelProvider(config).model

    val foreignKeyInfo = new ForeignKeyInfo(model)

    new RouteGenerator(model, foreignKeyInfo).writeToFile()

    ApplicationControllerGenerator.writeToFile(outputFolder, controllersPackage)

    model.tables map { table =>
      new ControllerGenerator(table, modelsPackage, foreignKeyInfo).writeToFile(outputFolder, controllersPackage)
    }
  }
}

class ControllerGenerator(table : Table, modelsPackage : String, foreignKeyInfo : ForeignKeyInfo) extends OutputHelpers with ControllerGeneratorHelpers with GeneratorHelpers {

  val mainTableInfo = new TableInfo(table)

  override val tableName: String = mainTableInfo.name

  val columns = mainTableInfo.columns

  val tableRowName = mainTableInfo.tableRowName

  override val formName = mainTableInfo.formName

  override val controllerName = mainTableInfo.controllerName

  override val daoObjectName = mainTableInfo.daoObjectName

  override val viewsPackage = mainTableInfo.viewsPackage

  override val primaryKeyName: String = mainTableInfo.primaryKeyName
  override val primaryKeyType: String = mainTableInfo.primaryKeyType

  override val parentDaoObjects: Seq[String] = table.foreignKeys.map { fk =>
    new TableInfo(foreignKeyInfo.tablesByName(fk.referencedTable)).daoObjectName
  }
  override val childsData: Seq[(String, String)] = {
    foreignKeyInfo.foreignKeysReferencedTable(table.name).map { fk =>
      val tabInfo = new TableInfo(foreignKeyInfo.tablesByName(fk.referencingTable))
      if(tabInfo.isJunctionTable){
        val foreignKeyToSecondSide = tabInfo.foreignKeys.filter(_.referencedTable != table.name).head
        val tableSecondSide = foreignKeyInfo.tablesByName(foreignKeyToSecondSide.referencedTable)
        val tableSecondSideInfo = new TableInfo(tableSecondSide)
        (tableSecondSideInfo.name, tableSecondSideInfo.daoObjectName)
      } else {
        (tabInfo.name, tabInfo.daoObjectName)
      }
    }
  }

  override def code: String = {
    s"""
${imports}

${objectCode(controllerName)}

""".trim()
  }

  override def indent(code: String): String = code

  def objectCode(objectName : String) : String = {
    s"""
object ${objectName} extends Controller {

  ${form}

  ${methods}

}""".trim()
  }

  def imports : String = {
    Seq(importCode("play.api.mvc.Controller"),
        importCode("play.api.mvc.Action"),
        importCode("play.api.data._"),
        importCode("play.api.data.Forms._"),
        importCode("play.api.data.format.Formats._"),
        importCode("utils.CustomFormats._"),
        importCode(modelsPackage + "._"),
        importCode(modelsPackage + ".Tables._"))
        .mkString("\n")
  }

  def methods : String = {
    if(mainTableInfo.isJunctionTable) methodsForJunctionTable.mkString("\n\n")
    else methodsForSimpleTable.mkString("\n\n")
  }
  
  def methodsForSimpleTable = {
    val childTablesInfo = foreignKeyInfo.parentChildrenTablesInfo(table.name)
    val deleteJunctionMethods = childTablesInfo.filter(_.isJunctionTable).map(deleteJunctionMethod(_))

    Seq(indexMethod,
      listMethod,
      createMethod,
      saveMethod,
      showMethod,
      editMethod,
      updateMethod,
      deleteMethod) ++ deleteJunctionMethods
  }
  
  def methodsForJunctionTable = Seq(indexJunctionMethod,
                                    createMethod,
                                    saveJunctionMethod)

  def form = {
    "val " + formName + " = " + formObject
  }

  def formObject = {

    s"""
Form(
      mapping(
          ${(columns map printFormField).mkString(",\n\t\t\t\t\t")}
          )(${tableRowName}.apply)(${tableRowName}.unapply)
      )""".trim()

  }

  def printFormField(field : Column) = {
    "\"" + standardColumnName(field.name) + "\"" + " -> " + typeCode(field)
  }

  def typeCode(field : Column) = {
    if(field.nullable)
      "optional(" + convertTypeToMapping(field.tpe) + ")"
    else convertTypeToMapping(field.tpe)
  }

  def convertTypeToMapping(tpe : String) = {
    tpe match {
      case "String" => "text"
      case "Int" => "number"
      case "Long" => "longNumber"
      case "scala.math.BigDecimal" => "bigDecimal"
      case "java.sql.Date" => "sqlDate"
      case "Boolean" => "boolean"
      case "Byte" => "of[Byte]"   // need formatter
      case "Short" => "of[Short]"  // need formatter
      case "Float" => "of[Float]"
      case "Double" => "of[Double]"
      case "java.sql.Blob" => "of[java.sql.Blob]" // need formatter
      case "java.sql.Time" => "of[java.sql.Time]" // need formatter
      case "java.sql.Timestamp" => "of[java.sql.Timestamp]" // need formatter
      case "java.sql.Clob" => "of[java.sql.Clob]" // need formatter
      case _ => "text"
    }
  }

  override def writeToFile(folder:String, pkg: String, fileName: String= controllerName +  ".scala") {
      super.writeToFile(folder, pkg, fileName)
    }

}

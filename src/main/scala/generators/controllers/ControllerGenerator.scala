package generators.controllers

import scala.slick.model.{ForeignKey, Column, Table}
import generators.utils._

object ControllerGenerator {
  def generate(outputFolder : String) = {

    val appConfig = AppConfigParser.getAppConfig

    val controllersPackage = appConfig.controllersPackage

    val model = new ModelProvider(appConfig).model

    val foreignKeyInfo = new ForeignKeyInfo(model)

    new RouteGenerator(model, foreignKeyInfo).writeToFile()

    ApplicationControllerGenerator.writeToFile(outputFolder, controllersPackage)

    model.tables map { table =>
      new ControllerGenerator(table, foreignKeyInfo).writeToFile(outputFolder, controllersPackage)
    }
  }
}

class ControllerGenerator(table : Table, foreignKeyInfo : ForeignKeyInfo) extends OutputHelpers with ControllerGeneratorHelpers with GeneratorHelpers {

  val mainTableInfo = new TableInfo(table)

  val modelsPackage = AppConfigParser.getAppConfig.modelsPackage

  override val tableName: String = mainTableInfo.name

  val columns = mainTableInfo.columns

  val tableRowName = mainTableInfo.tableRowName

  override val formName = mainTableInfo.formName

  override val controllerName = mainTableInfo.controllerName

  override val daoObjectName = mainTableInfo.daoObjectName

  override val viewsPackage = mainTableInfo.viewsPackage

  override val primaryKeyColumns: Seq[Column] = mainTableInfo.primaryKeyColumns

  override val parentDaoObjectsAndReferencedColumn: Seq[(TableInfo, Column)] = {
    table.foreignKeys.map(fk => (fk.referencedTable, fk.referencedColumns.head)).distinct.map{ tup =>
      (new TableInfo(foreignKeyInfo.tablesByName(tup._1)), tup._2)
    }
  }

  override val childsData: Seq[(TableInfo, ForeignKey)] = {
    foreignKeyInfo.foreignKeysReferencedTable(table.name).map { fk =>
      val tabInfo = new TableInfo(foreignKeyInfo.tablesByName(fk.referencingTable))
      if(tabInfo.isJunctionTable || tabInfo.isSimpleJunctionTable){
        val foreignKeyToSecondSide = tabInfo.foreignKeys.filter(_.referencedTable != table.name).head
        val tableSecondSide = foreignKeyInfo.tablesByName(foreignKeyToSecondSide.referencedTable)
        val tableSecondSideInfo = new TableInfo(tableSecondSide)
        (tableSecondSideInfo, fk)
      } else {
        (tabInfo, fk)
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
    if(mainTableInfo.isSimpleJunctionTable) methodsForSimpleJunctionTable.mkString("\n\n")
    else methodsForSimpleTable.mkString("\n\n")
  }
  
  def methodsForSimpleTable = {
    val childTablesInfo = foreignKeyInfo.parentChildrenTablesInfo(table.name)
    val deleteJunctionMethods = childTablesInfo.filter(_.isSimpleJunctionTable).map(deleteSimpleJunctionMethod(_))

    Seq(indexMethod,
      listMethod,
      createMethod,
      saveMethod,
      showMethod,
      editMethod,
      updateMethod,
      deleteMethod) ++ deleteJunctionMethods
  }
  
  def methodsForSimpleJunctionTable = Seq(indexJunctionMethod,
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

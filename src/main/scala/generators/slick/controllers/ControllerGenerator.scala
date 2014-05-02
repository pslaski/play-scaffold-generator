package generators.slick.controllers

import scala.slick.model.Column
import scala.slick.model.Table
import generators.slick.utils.{TableInfo, DriverLoader, SlickGeneratorHelpers}
import generators.utils.{Config, OutputHelpers}

object ControllerGenerator {
  def generate(config : Config, outputFolder : String) = {

      val jdbcDriver = config.jdbcDriver
      val url = config.url
      val modelsPackage = config.modelsPackage
      val controllersPackage = config.controllersPackage
      val user = config.user
      val password = config.password

      val slickDriver = DriverLoader.slickDriver(jdbcDriver)

      val db = slickDriver.simple.Database.forURL(url,driver=jdbcDriver, user = user, password = password)
      val model = db.withSession(slickDriver.createModel(_))

      model.tables map { table =>
        new ControllerGenerator(table, modelsPackage).writeToFile(outputFolder, controllersPackage)
      }
    }
}

class ControllerGenerator(table : Table, modelsPackage : String) extends OutputHelpers with ControllerGeneratorHelpers with SlickGeneratorHelpers {

  val tableInfo = new TableInfo(table)

  val columns = tableInfo.columns

  val tableRowName = tableInfo.tableRowName

  override val formName = tableInfo.formName

  override val controllerName = tableInfo.controllerName

  override val daoObjectName = tableInfo.daoObjectName

  override val viewsPackage = tableInfo.viewsPackage

  override val primaryKeyName: String = tableInfo.primaryKeyName
  override val primaryKeyType: String = tableInfo.primaryKeyType

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
        importCode(modelsPackage + "._"),
        importCode(modelsPackage + ".Tables._"))
        .mkString("\n")
  }

  def methods : String = {
    Seq(indexMethod,
        listMethod,
        createMethod,
        saveMethod,
        showMethod,
        editMethod,
        updateMethod,
        deleteMethod).mkString("\n\n")
  }

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
    "\"" + field.name + "\"" + " -> " + typeCode(field)
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
      case "BigDecimal" => "bigDecimal"
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
      val routeGenerator = new RouteGenerator(table)
      routeGenerator.appendToFile("conf", "routes")
      writeStringToFile(packageCode(pkg), folder, pkg, fileName)
    }
}

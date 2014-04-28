package generators.slick.controllers

import scala.slick.model.Column
import scala.slick.model.Table
import generators.slick.utils.{DriverLoader, SlickGeneratorHelpers}
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

  val tableName = table.name.table.toCamelCase

  override val columns = table.columns

  val tableRowName = tableName + "Row"

  override val formName = tableName + "Form"

  override val controllerName = tableName + "Controller"

  override val daoObjectName = table.name.table.toCamelCase + "Dao"

  override val viewsPackage = table.name.table.toLowerCase

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
      case "Byte" => "of[Byte]"
      case "Short" => "of[Short]"
      case "Float" => "of[Float]"
      case "Double" => "of[Double]"
      case "java.sql.Blob" => "of[java.sql.Blob]"
      case "java.sql.Time" => "of[java.sql.Time]"
      case "java.sql.Timestamp" => "of[java.sql.Timestamp]"
      case "java.sql.Clob" => "of[java.sql.Clob]"
      case _ => "text"
    }
  }

  override def writeToFile(folder:String, pkg: String, fileName: String= controllerName +  ".scala") {
      val routeGenerator = new RouteGenerator(tableName, controllerName, primaryKeyType)
      routeGenerator.appendToFile("conf", "routes")
      writeStringToFile(packageCode(pkg), folder, pkg, fileName)
    }

}

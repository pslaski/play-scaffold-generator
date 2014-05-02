package generators.slick.controllers

import generators.utils.OutputHelpers
import scala.slick.model.Table
import generators.slick.utils.TableInfo

class RouteGenerator(table : Table) extends OutputHelpers {

  val tableInfo = new TableInfo(table)

  override def code: String = {
    Seq(comment,
        indexRoute,
        listRoute,
        createRoute,
        saveRoute,
        editRoute,
        updateRoute,
        showRoute,
        deleteRoute).mkString("\n")
  }

  override def indent(code: String): String = "\n" + code + "\n"

  val tableUrl = tableInfo.name + "s"

  val controllerName = tableInfo.controllerName

  val primaryKeyType = tableInfo.primaryKeyType

  val separator = "\t\t"

  def comment = {
    s"# ${tableInfo.nameCamelCased} routes"
  }

  def indexRoute = {
    s"GET${separator}/${tableUrl}${separator}controllers.${controllerName}.index"
  }

  def listRoute = {
    s"GET${separator}/${tableUrl}/list${separator}controllers.${controllerName}.list"
  }

  def createRoute = {
    s"GET${separator}/${tableUrl}/new${separator}controllers.${controllerName}.create"
  }

  def saveRoute = {
    s"POST${separator}/${tableUrl}/save${separator}controllers.${controllerName}.save"
  }

  def editRoute = {
    s"GET${separator}/${tableUrl}/edit/:id${separator}controllers.${controllerName}.edit(id: ${primaryKeyType})"
  }

  def updateRoute = {
    s"POST${separator}/${tableUrl}/update${separator}controllers.${controllerName}.update"
  }

  def showRoute = {
    s"GET${separator}/${tableUrl}/show/:id${separator}controllers.${controllerName}.show(id: ${primaryKeyType})"
  }

  def deleteRoute = {
    s"GET${separator}/${tableUrl}/delete/:id${separator}controllers.${controllerName}.delete(id: ${primaryKeyType})"
  }
}

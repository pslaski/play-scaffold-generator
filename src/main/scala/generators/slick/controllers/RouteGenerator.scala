package generators.slick.controllers

import generators.utils.OutputHelpers
import scala.slick.model.Table
import generators.slick.utils.{ForeignKeyInfo, TableInfo}

class RouteGenerator(table : Table, foreignKeyInfo : ForeignKeyInfo) extends OutputHelpers {

  val tableInfo = new TableInfo(table)

  override def code: String = {
    if(tableInfo.isJunctionTable) {
      routesForJunctionTable.mkString("\n")
    }
    else {
      routesForSimpleTable.mkString("\n")
    }
  }

  override def indent(code: String): String = "\n" + code + "\n"

  lazy val routesForSimpleTable = {
    val deleteJunctionsRoutes = foreignKeyInfo.parentChildrenTablesInfo(table.name).filter(_.isJunctionTable).map(deleteJunctionRoute(_))

    Seq(comment,
        indexRoute,
        listRoute,
        createRoute,
        saveRoute,
        editRoute,
        updateRoute,
        showRoute,
        deleteRoute) ++ deleteJunctionsRoutes
  }

  lazy val routesForJunctionTable = {
    Seq(comment,
        indexRoute,
        createRoute,
        saveRoute)
  }

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

  def deleteJunctionRoute(tabInfo : TableInfo) = {
    val methodName = "delete" + tabInfo.nameCamelCased

    val idColumns = tabInfo.foreignKeys.map{ fk =>
      fk.referencingColumns.map(col => col.name + " : " + col.tpe)
    }.flatten.mkString(", ")

    s"GET${separator}/${tableUrl}/${methodName}${separator}controllers.${controllerName}.${methodName}(${idColumns})"
  }
}
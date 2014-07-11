package generators.controllers

import generators.utils.{GeneratorHelpers, TableInfo, ForeignKeyInfo, OutputHelpers}
import scala.slick.model.{Column, Model, Table}

class RouteGenerator(model : Model, foreignKeyInfo : ForeignKeyInfo) extends OutputHelpers {

  override def code: String = {
s"""
# Routes
# This file defines all application routes (Higher priority routes first)
# ~~~~

# Home page
GET     /                           controllers.Application.index

# Map static resources from the /public folder to the /assets URL path
GET     /assets/*file               controllers.Assets.at(path="/public", file)

${routes}
 """.trim
  }

  override def indent(code: String): String = code

  override def writeToFile(folder:String = "conf", pkg: String = "", fileName: String="routes") {
    writeStringToFile(indent(code), folder, pkg, fileName)
  }

  val routes = {
    model.tables.map{ table =>
      new TableRouteGenerator(table, foreignKeyInfo).routes
    }.mkString("\n\n")
  }
}

class TableRouteGenerator(table : Table, foreignKeyInfo : ForeignKeyInfo) extends GeneratorHelpers{

  val tableInfo = new TableInfo(table)

  val primaryKeyColumns: Seq[Column] = tableInfo.primaryKeyColumns

  def routes = {
      if(tableInfo.isSimpleJunctionTable) {
        routesForSimpleJunctionTable.mkString("\n")
      }
      else {
        routesForSimpleTable.mkString("\n")
      }
    }

  lazy val routesForSimpleTable = {

    val columnsReferenced = foreignKeyInfo.foreignKeysReferencedTable(table.name).map(_.referencedColumns).distinct

    val uniqueShowByRoutes = columnsReferenced.filterNot(_.equals(primaryKeyColumns)).map(cols => showByRoute(cols))

    val deleteJunctionsRoutes = foreignKeyInfo.parentChildrenTablesInfo(table.name).filter(_.isSimpleJunctionTable).map(deleteJunctionRoute(_))

    Seq(comment,
        indexRoute,
        listRoute,
        createRoute,
        saveRoute,
        editRoute,
        updateRoute,
        showRoute,
        deleteRoute) ++ uniqueShowByRoutes ++ deleteJunctionsRoutes
  }

  lazy val routesForSimpleJunctionTable = {
    Seq(comment,
        indexRoute,
        createRoute,
        saveRoute)
  }

  val tableUrl = tableInfo.name + "s"

  lazy val urlArgs = makeUrlArgs(primaryKeyColumns)

  private def makeUrlArgs(columns : Seq[Column]) = {
    if(columns.length == 1){
      ":" + standardColumnName(columns.head.name)
    } else ""
  }

  val controllerName = tableInfo.controllerName

  val methodArgs = makeArgsWithTypes(primaryKeyColumns)

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
    s"GET${separator}/${tableUrl}/edit/${urlArgs}${separator}controllers.${controllerName}.edit(${methodArgs})"
  }

  def updateRoute = {
    s"POST${separator}/${tableUrl}/update${separator}controllers.${controllerName}.update"
  }

  def showRoute = {
    s"GET${separator}/${tableUrl}/show/${urlArgs}${separator}controllers.${controllerName}.show(${methodArgs})"
  }

  def deleteRoute = {
    s"GET${separator}/${tableUrl}/delete/${urlArgs}${separator}controllers.${controllerName}.delete(${methodArgs})"
  }

  def showByRoute(columns : Seq[Column]) = {

    val showByMethodName = makeShowByMethodName(columns)

    val showUrlArgs = makeUrlArgs(columns)

    val showMethodArgs = makeArgsWithTypes(columns)

    s"GET${separator}/${tableUrl}/${showByMethodName}/${showUrlArgs}${separator}controllers.${controllerName}.${showByMethodName}(${showMethodArgs})"
  }

  def deleteJunctionRoute(tabInfo : TableInfo) = {
    val methodName = "delete" + tabInfo.nameCamelCased

    val idColumns = tabInfo.foreignKeys.map{ fk =>
      fk.referencingColumns.map(col => col.name + " : " + col.tpe)
    }.flatten.mkString(", ")

    s"GET${separator}/${tableUrl}/${methodName}${separator}controllers.${controllerName}.${methodName}(${idColumns})"
  }
}
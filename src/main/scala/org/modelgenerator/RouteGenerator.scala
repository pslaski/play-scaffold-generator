package org.modelgenerator

class RouteGenerator(tableName : String, controllerName : String, primaryKeyType : String) extends OutputHelpers {

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

  val tableUrl = tableName.toLowerCase + "s"

  val separator = "\t\t"

  def comment = {
    s"# ${tableName} routes"
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

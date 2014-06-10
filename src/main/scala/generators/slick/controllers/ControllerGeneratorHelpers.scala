package generators.slick.controllers

import generators.utils.StringUtils
import generators.slick.utils.TableInfo

trait ControllerGeneratorHelpers extends StringUtils{

  val tableName : String

  val controllerName : String

  val viewsPackage : String

  val daoObjectName : String

  val formName : String

  val primaryKeyName : String

  val primaryKeyType : String

  val parentDaoObjects : Seq[String]

  val childsData : Seq[(String, String)]

  def indexMethod = {
    s"""
def index = Action {
  Redirect(routes.${controllerName}.list)
}""".trim()
  }

  def indexJunctionMethod = {
    s"""
def index = Action {
  Redirect(routes.${controllerName}.create)
}""".trim()
  }

  def listMethod = {
    s"""
def list = Action {
  Ok(views.html.${viewsPackage}.list(${daoObjectName}.findAll))
}""".trim()
  }

  def createMethod = {
    s"""
def create = Action { implicit request =>
  Ok(views.html.${viewsPackage}.createForm(${formName}${formOptions}))
}""".trim()
  }

  def saveMethod = {
    s"""
def save = Action { implicit request =>
  ${formName}.bindFromRequest.fold(
      formWithErrors => {
      BadRequest(views.html.${viewsPackage}.createForm(formWithErrors${formOptions}))
    },
    formData => {
      val id = ${daoObjectName}.save(formData)
      Redirect(routes.${controllerName}.show(id))
    }
  )
}""".trim()
  }

  def saveJunctionMethod = {
    s"""
def save = Action { implicit request =>
  ${formName}.bindFromRequest.fold(
      formWithErrors => {
      BadRequest(views.html.${viewsPackage}.createForm(formWithErrors${formOptions}))
    },
    formData => {
      ${daoObjectName}.save(formData)
      Redirect(routes.${controllerName}.create).flashing("success" -> "${tableName.toCamelCase} saved")
    }
  )
}""".trim()
  }

  def showMethod = {
    s"""
def show(${primaryKeyName} : ${primaryKeyType}) = Action {
  ${daoObjectName}.findById(${primaryKeyName}).fold(
    BadRequest("Not existed")
  ){
    obj => {
      ${childsFinders}
      Ok(views.html.${viewsPackage}.show(obj${showViewOptions}))
    }
  }
}""".trim()
  }

  def childsFinders = {
    childsData.map{ child =>
      childFinder(child._1, child._2)
    }.mkString("\n")
  }

  private def childFinder(child : String, childDao : String) = {
    val childName = child.toCamelCase.uncapitalize + "s"
    s"val ${childName} = ${childDao}.${childName}For${tableName.toCamelCase}(obj)"
  }

  def editMethod = {
    s"""
def edit(${primaryKeyName} : ${primaryKeyType}) = Action {
  ${daoObjectName}.findById(${primaryKeyName}).map { obj =>
      Ok(views.html.${viewsPackage}.editForm(${formName}.fill(obj)${formOptions}))
  }.getOrElse(NotFound)
}""".trim()
  }

  def updateMethod = {
    s"""
def update = Action { implicit request =>
  ${formName}.bindFromRequest.fold(
      formWithErrors => {
      BadRequest(views.html.${viewsPackage}.editForm(formWithErrors${formOptions}))
    },
    formData => {
      ${daoObjectName}.update(formData)
      Redirect(routes.${controllerName}.show(formData.${primaryKeyName}))
    }
  )
}""".trim()
  }

  def deleteMethod = {
    s"""
def delete(${primaryKeyName} : ${primaryKeyType}) = Action {
  ${daoObjectName}.delete(${primaryKeyName})
  Redirect(routes.${controllerName}.list)
}""".trim()
  }

  def deleteJunctionMethod(junctionTableInfo : TableInfo) = {

    val idColumns = junctionTableInfo.foreignKeys.map{ fk =>
      fk.referencingColumns.map( col => col.name + " : " + col.tpe)
    }.flatten.mkString(", ")

    val deleteArgs = junctionTableInfo.foreignKeys.map{ fk =>
      fk.referencingColumns.map(_.name)
    }.flatten.mkString(", ")

    s"""
def delete${junctionTableInfo.nameCamelCased}(${idColumns}) = Action {
  ${junctionTableInfo.daoObjectName}.delete(${deleteArgs})
  Redirect(routes.${controllerName}.show(${primaryKeyName}))
}
""".trim()
  }

  private def showViewOptions = {
    if(childsData.isEmpty){
      ""
    } else {
      ", " + childsData.map(_._1.toCamelCase.uncapitalize + "s").mkString(", ")
    }
  }

  private def formOptions = {
    if(parentDaoObjects.isEmpty){
      ""
    } else {
      ", " + parentDaoObjects.map(_ + ".formOptions").mkString(", ")
    }
  }
}

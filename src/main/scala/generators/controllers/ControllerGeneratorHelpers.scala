package generators.controllers

import generators.utils.{GeneratorHelpers, TableInfo, StringUtils}

import scala.slick.model.{ForeignKey, Column}

trait ControllerGeneratorHelpers extends GeneratorHelpers{

  val tableName : String

  val controllerName : String

  val viewsPackage : String

  val daoObjectName : String

  val formName : String

  val primaryKeyName : String

  val primaryKeyType : String

  val primaryKeyColumns : Seq[Column]

  val parentDaoObjectsAndReferencedColumn : Seq[(TableInfo, Column)]

  val childsData : Seq[(TableInfo, ForeignKey)]

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

    val showArgs = primaryKeyColumns.map(col => "row." + standardColumnName(col.name)).mkString(", ")

    s"""
def save = Action { implicit request =>
  ${formName}.bindFromRequest.fold(
      formWithErrors => {
      BadRequest(views.html.${viewsPackage}.createForm(formWithErrors${formOptions}))
    },
    formData => {
      val row = ${daoObjectName}.save(formData)
      Redirect(routes.${controllerName}.show(${showArgs}))
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
def show(${makeArgsWithTypes(primaryKeyColumns)}) = Action {
  ${daoObjectName}.findByPrimaryKey(${makeArgsWithoutTypes(primaryKeyColumns)}).fold(
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

  private def childFinder(child : TableInfo, fk : ForeignKey) = {
    val childName = child.nameCamelCasedUncapitalized + "sBy" + makeColumnsAndString(fk.referencingColumns)

    val findMethod = makeFindByMethodName(fk.referencingColumns)

    val findMethodArgs = fk.referencedColumns.map(col => "obj." + standardColumnName(col.name)).mkString(", ")

    s"val ${childName} = ${child.daoObjectName}.${findMethod}(${findMethodArgs})"
  }

  def editMethod = {
    s"""
def edit(${makeArgsWithTypes(primaryKeyColumns)}) = Action {
  ${daoObjectName}.findByPrimaryKey(${makeArgsWithoutTypes(primaryKeyColumns)}).map { obj =>
      Ok(views.html.${viewsPackage}.editForm(${formName}.fill(obj)${formOptions}))
  }.getOrElse(NotFound)
}""".trim()
  }

  def updateMethod = {

    val showArgs = primaryKeyColumns.map(col => standardColumnName(col.name)).map("formData." + _).mkString(", ")

    s"""
def update = Action { implicit request =>
  ${formName}.bindFromRequest.fold(
      formWithErrors => {
      BadRequest(views.html.${viewsPackage}.editForm(formWithErrors${formOptions}))
    },
    formData => {
      ${daoObjectName}.update(formData)
      Redirect(routes.${controllerName}.show(${showArgs}))
    }
  )
}""".trim()
  }

  def deleteMethod = {
    s"""
def delete(${makeArgsWithTypes(primaryKeyColumns)}) = Action {
  ${daoObjectName}.delete(${makeArgsWithoutTypes(primaryKeyColumns)})
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

    val parentPk = junctionTableInfo.foreignKeys.filter(_.referencedTable.table.equals(tableName)).head.referencingColumns.head.name

    s"""
def delete${junctionTableInfo.nameCamelCased}(${idColumns}) = Action {
  ${junctionTableInfo.daoObjectName}.delete(${deleteArgs})
  Redirect(routes.${controllerName}.show(${parentPk}))
}
""".trim()
  }

  private def showViewOptions = {
    if(childsData.isEmpty){
      ""
    } else {
      ", " + childsData.map(data => data._1.nameCamelCasedUncapitalized + "sBy" + makeColumnsAndString(data._2.referencingColumns)).mkString(", ")
    }
  }

  private def formOptions = {
    if(parentDaoObjectsAndReferencedColumn.isEmpty){
      ""
    } else {
      ", " + parentDaoObjectsAndReferencedColumn.map(data => data._1.daoObjectName + ".formOptionsBy" + makeColumnsAndString(Seq(data._2))).mkString(", ")
    }
  }
}

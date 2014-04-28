package generators.slick.controllers

trait ControllerGeneratorHelpers {

  val controllerName : String

  val viewsPackage : String

  val daoObjectName : String

  val formName : String

  val primaryKeyName : String

  val primaryKeyType : String

  def indexMethod = {
    s"""
def index = Action {
  Redirect(routes.${controllerName}.list)
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
def create = Action {
  Ok(views.html.${viewsPackage}.createForm(${formName}))
}""".trim()
  }

  def saveMethod = {
    s"""
def save = Action { implicit request =>
  ${formName}.bindFromRequest.fold(
      formWithErrors => {
      BadRequest(views.html.${viewsPackage}.createForm(formWithErrors))
    },
    formData => {
      val id = ${daoObjectName}.save(formData)
      Redirect(routes.${controllerName}.show(id))
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
      Ok(views.html.${viewsPackage}.show(obj))
    }
  }
}""".trim()
  }

  def editMethod = {
    s"""
def edit(${primaryKeyName} : ${primaryKeyType}) = Action {
  ${daoObjectName}.findById(${primaryKeyName}).map { obj =>
      Ok(views.html.${viewsPackage}.editForm(${formName}.fill(obj)))
  }.getOrElse(NotFound)
}""".trim()
  }

  def updateMethod = {
    s"""
def update = Action { implicit request =>
  ${formName}.bindFromRequest.fold(
      formWithErrors => {
      BadRequest(views.html.${viewsPackage}.editForm(formWithErrors))
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

}

package generators.slick.views

import scala.slick.model.{Column, Table}

class EditFormViewGenerator(table : Table) extends ViewHelpers with FormViewGeneratorHelpers {

  override val columns: Seq[Column] = table.columns

  val tableName = table.name.table

  override val title: String = "Edit " + tableName

  val tableRowName = tableName.toCamelCase + "Row"

  val formName = tableName + "Form"

  val controllerName = tableName.toCamelCase + "Controller"

  override val submitButtonText: String = "Update"

  override val formAction: String = "update"

  override val arguments = Seq((formName, "Form[Tables." + tableRowName + "]"))

  override def imports: String = {
    Seq(importCodeView("helper._"),
        importCodeView("helper.twitterBootstrap._")).mkString("\n")
  }

  override def bodyCode: String = {
    s"""
<h1>Edit ${tableName}</h1>
  ${form}
""".trim()
  }
}

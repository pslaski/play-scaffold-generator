package generators.slick.views

import scala.slick.model.{Column, Table}
import generators.slick.utils.TableInfo

class CreateFormViewGenerator(table : Table) extends ViewHelpers with FormViewGeneratorHelpers{

  val tableInfo = new TableInfo(table)

  override val columns: Seq[Column] = tableInfo.columns

  val tableName = tableInfo.name

  override val title: String = "Add new " + tableName

  val tableRowName = tableInfo.tableRowName

  val formName = tableInfo.formName

  val controllerName = tableInfo.controllerName

  override val submitButtonText: String = "Save"

  override val formAction: String = "save"

  override val arguments = Seq((formName, "Form[Tables." + tableRowName + "]"))

  override val primaryKeyName: String = tableInfo.primaryKeyName

  override val primaryKeyDefaultValue = "0"

  override def imports: String = {
    Seq(importCodeView("helper._"),
        importCodeView("helper.twitterBootstrap._")).mkString("\n")
  }

  override def bodyCode: String = {
    s"""
<h1>Add new ${tableName}</h1>
  ${form}
""".trim()
  }
}

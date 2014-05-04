package generators.slick.views

import scala.slick.model.{Column, Table}
import generators.slick.utils.TableInfo

class EditFormViewGenerator(table : Table) extends ViewHelpers with FormViewGeneratorHelpers {

  val tableInfo = new TableInfo(table)

  override val columns: Seq[Column] = tableInfo.columns

  val tableName = tableInfo.name

  override val title: String = "Edit " + tableName

  val tableRowName = tableInfo.tableRowName

  val formName = tableInfo.formName

  val controllerName = tableInfo.controllerName

  override val primaryKeyName: String = tableInfo.primaryKeyName

  override val submitButtonText: String = "Update"

  override val formAction: String = "update"

  override val foreignKeys: Seq[(String, String)] = tableInfo.foreignKeys.map { fk =>
    (fk.referencingColumns.head.name, fk.referencedTable.table.toCamelCase.uncapitalize)
  }

  val selectFormOptionsArgs : Seq[(String, String)] = foreignKeys.map( fk => ( fk._2 + "Options", "Seq[(String, String)]"))

  override val arguments = Seq((formName, "Form[Tables." + tableRowName + "]")) ++ selectFormOptionsArgs

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

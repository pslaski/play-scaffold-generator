package generators.views

import generators.utils.TableInfo

import scala.slick.model.{Column, Table}

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

  override val foreignKeys: Seq[(String, String)] = tableInfo.foreignKeys.map { fk =>
    (fk.referencingColumns.head.name, fk.referencedTable.table.toCamelCase.uncapitalize)
  }

  val selectFormOptionsArgs : Seq[(String, String)] = foreignKeys.map( fk => ( fk._2 + "Options", "Seq[(String, String)]"))

  override val arguments = Seq((formName, "Form[Tables." + tableRowName + "]")) ++ selectFormOptionsArgs

  override val primaryKeyName: String = tableInfo.primaryKeyName

  override val primaryKeyDefaultValue = "0"

  override def imports: String = {
    Seq(importCodeView("helper._"),
        importCodeView("helper.twitterBootstrap._")).mkString("\n")
  }

  override def bodyCode: String = {
    s"""
<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">Add new ${tableName}</h3>
    </div>
    <div class="panel-body">
        ${form}
    </div>
</div>
""".trim()
  }

}
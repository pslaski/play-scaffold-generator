package generators.views

import generators.utils.TableInfo

import scala.slick.model.{ForeignKey, Column, Table}

class EditFormViewGenerator(table : Table) extends ViewHelpers with FormViewGeneratorHelpers {

  val tableInfo = new TableInfo(table)

  override val columns: Seq[Column] = tableInfo.columns

  val tableName = tableInfo.nameCamelCasedUncapitalized

  override val title: String = "Edit " + tableName

  val tableRowName = tableInfo.tableRowName

  val formName = tableInfo.formName

  val controllerName = tableInfo.controllerName

  override val submitButtonText: String = "Update"

  override val formAction: String = "update"

  override val foreignKeys: Seq[ForeignKey] = tableInfo.foreignKeys

  val selectFormOptionsArgs : Seq[(String, String)] = {
    foreignKeys.map(fk => (fk.referencedTable, fk.referencedColumns)).distinct.map{ tup =>
      val optName = tup._1.table.toCamelCase.uncapitalize + "OptionsBy" + makeColumnsAndString(tup._2)
      (optName, "Seq[(String, String)]")
    }
  }

  override val arguments = Seq((formName, "Form[Tables." + tableRowName + "]")) ++ selectFormOptionsArgs

  override def imports: String = {
    Seq(importCodeView("helper._"),
        importCodeView("helper.twitterBootstrap._")).mkString("\n")
  }

  override def bodyCode: String = {
    s"""
<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">Edit ${tableName}</h3>
    </div>
    <div class="panel-body">
        ${form}
    </div>
</div>
""".trim()
  }

}

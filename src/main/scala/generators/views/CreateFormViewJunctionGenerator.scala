package generators.views

import generators.utils.TableInfo

import scala.slick.model.{Column, Table}

class CreateFormViewJunctionGenerator(table : Table) extends ViewHelpers with FormViewGeneratorHelpers{

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

  override def viewArguments = super.viewArguments + "(implicit flash: Flash)"

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
    @if(!flash.isEmpty){
        <div class="alert alert-success alert-dismissable">
            <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
            @flash.get("success")
        </div>
    }
    <div class="panel-body">
        ${form}
    </div>
</div>
""".trim()
  }

  override def formField(column : Column) = {
    if(foreignKeys.exists(_._1.equals(column.name))){
      selectCode(column.name.toLowerCase, foreignKeys.find(_._1.equals(column.name)).get._2)
    }
    else {
      convertTypeToInput(column)
    }
  }

  override def actions = {
    s"""
<div class="form-group actions">
    <div class="col-sm-10">
        <button type="submit" class="btn btn-success">${submitButtonText}</button>
        <a href="@routes.Application.index" class="btn btn-default">Cancel</a>
    </div>
</div>
""".trim()
  }

}
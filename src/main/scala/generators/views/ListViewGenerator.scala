package generators.views

import generators.utils.{TableInfo, GeneratorHelpers}

import scala.slick.model.{Table, Column}

class ListViewGenerator(table : Table) extends ViewHelpers with GeneratorHelpers{

  val tableInfo = new TableInfo(table)

  val columns: Seq[Column] = tableInfo.listColumns

  val tableName = tableInfo.nameCamelCasedUncapitalized

  override val title: String = tableName + " list"

  val tableRowName = tableInfo.tableRowName

  val listName = tableInfo.listName

  val controllerName = tableInfo.controllerName

  val primaryKeyColumns: Seq[Column] = tableInfo.primaryKeyColumns

  val buttonsArgs = primaryKeyColumns.map(col => tableName + "." + standardColumnName(col.name)).mkString(", ")

  override val arguments = Seq((listName, "List[Tables." + tableRowName + "]"))

  override def imports: String = ""

  override def bodyCode: String = {
    s"""
<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">${listName.toUpperCase}</h3>
        <div class="pull-right btn-group">
            <a href="@routes.${controllerName}.create" class="btn btn-primary btn-xs">Add new ${tableName}</a>
        </div>
    </div>
    <div class="panel-body">
        <table class="table table-hover table-bordered table-responsive table-middle">
            <thead>
                ${headers}
            </thead>
            <tbody>
            @for(${tableName} <- ${listName}) {
                <tr>
                    ${rows}
                    <td class="text-center">
                        ${buttons}
                    </td>
                </tr>
            }
            </tbody>
        </table>
    </div>
</div>
""".trim()
  }

  def headers = {
    (columns.map("<th>" + _.name + "</th>") :+ "<th class=\"text-center\">Actions</th>").mkString("\n")
  }

  def rows = {
    (columns map { col =>
      if(col.nullable)  printOptionalField(standardColumnName(col.name))
      else printField(standardColumnName(col.name))
    }).mkString("\n")
  }

  def printField(field : String) = {
    s"<td>@${tableName}.${field}</td>"
  }

  def printOptionalField(field : String) = {
    s"""
<td>@${tableName}.${field}.map { ${field} =>
      @${field}
    }
</td>
""".trim()
  }

  def buttons = {
    s"""
<div class="btn-group">
  <a href="@routes.${controllerName}.show(${buttonsArgs})" class="btn btn-success">Show</a>
  <a href="@routes.${controllerName}.edit(${buttonsArgs})" class="btn btn-warning">Edit</a>
  <a href="@routes.${controllerName}.delete(${buttonsArgs})" class="btn btn-danger">Delete</a>
</div>
""".trim()
  }

}

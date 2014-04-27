package org.modelgenerator

import scala.slick.model.{Table, Column}

class ListViewGenerator(table : Table) extends GeneratorHelpers with ViewHelpers {

  override val columns: Seq[Column] = table.columns

  val tableName = table.name.table

  override val title: String = tableName + " list"

  val tableRowName = tableName.toCamelCase + "Row"

  val listName = tableName + "s"

  val controllerName = tableName.toCamelCase + "Controller"

  override val arguments = Seq((listName, "List[Tables." + tableRowName + "]"))

  override def imports: String = ""

  override def bodyCode: String = {
    s"""
<p><a href="@routes.${controllerName}.create" class="btn btn-primary">Add new ${tableName}</a></p>

<ul class="list-group">
    @for(${tableName} <- ${listName}) {
        <li class="list-group-item">

            ${row}

            ${buttons}
        </li>
    }
</ul>
""".trim()
  }


  def row = {
    (columns.take(5) map { col =>
      if(col.nullable)  printOptionalField(col.name)
      else s"@${tableName}.${col.name}"
    }).mkString(" ")
  }

  def printOptionalField(field : String) = {
    s"""
@${tableName}.${field}.map { ${field} =>
          @${field}
        }
""".trim()
  }

  def buttons = {
    s"""
<div class="btn-group">
  <a href="@routes.${controllerName}.show(${tableName}.${primaryKeyName})" class="btn btn-success">Show</a>
  <a href="@routes.${controllerName}.edit(${tableName}.${primaryKeyName})" class="btn btn-info">Edit</a>
  <a href="@routes.${controllerName}.delete(${tableName}.${primaryKeyName})" class="btn btn-danger">Delete</a>
</div>
""".trim()
  }

}

package generators.slick.views

import scala.slick.model.{Column, Table}
import generators.slick.utils.{ForeignKeyInfo, TableInfo}

class ShowViewGenerator(table : Table, foreignKeyInfo : ForeignKeyInfo) extends ViewHelpers {

  val tableInfo = new TableInfo(table)

  val columns: Seq[Column] = tableInfo.columns

  val tableName = tableInfo.name

  val foreignKeys = tableInfo.foreignKeys

  override val title: String = "Show " + tableName

  val tableRowName = tableInfo.tableRowName

  val controllerName = tableInfo.controllerName

  val primaryKeyName = tableInfo.primaryKeyName

  override val arguments = Seq((tableName, "Tables." + tableRowName))

  override def imports: String = ""

  override def bodyCode: String = {
    s"""
<h2>${tableName}</h2>
${fields}

${buttons}
""".trim()
  }

  def fields = {
    (columns map {col =>
      if(col.nullable) {
        if(isColumnForeignKey(col)){
          val parentTableInfo = new TableInfo(foreignKeyInfo.tablesByName(foreignKeys.find(_.referencingColumns.head.name.equals(col.name)).get.referencedTable))
          printOptionalForeignKeyField(parentTableInfo.nameCamelCased, parentTableInfo.controllerName, col.name)
        }
        else {
          s"<p>${col.name} : ${printOptionalField(col.name)}</p>"
        }
      }
      else {
        if(isColumnForeignKey(col)) {
          val parentTableInfo = new TableInfo(foreignKeyInfo.tablesByName(foreignKeys.find(_.referencingColumns.head.name.equals(col.name)).get.referencedTable))
          printForeignKeyField(parentTableInfo.nameCamelCased, parentTableInfo.controllerName, col.name)
        }
        else s"<p>${col.name} : @${tableName}.${col.name}</p>"
      }
    }).mkString("\n")
  }

  def isColumnForeignKey(column : Column) = {
    foreignKeys.exists(_.referencingColumns.head.name.equals(column.name))
  }

  def printOptionalField(field : String) = {
    s"""
@${tableName}.${field}.map { ${field} =>
          @${field}
        }
""".trim()
  }

  def printOptionalForeignKeyField(parentName: String, parentControllerName : String, foreignKey : String) = {
    s"""
@${tableName}.${foreignKey}.map { ${foreignKey} =>
          ${printForeignKeyField(parentName, parentControllerName, foreignKey)}
        }
""".trim()
  }

  def printForeignKeyField(parentName: String, parentControllerName : String, foreignKey : String) = {
    s"""<p>${parentName} : <a href="@routes.${parentControllerName}.show(${tableName}.${foreignKey})" class="btn btn-default">@${tableName}.${foreignKey}</a> </p>"""
  }


  def buttons = {
    s"""
<div class="btn-group">
  <a href="@routes.${controllerName}.list" class="btn btn-success">List</a>
  <a href="@routes.${controllerName}.edit(${tableName}.${primaryKeyName})" class="btn btn-info">Edit</a>
  <a href="@routes.${controllerName}.delete(${tableName}.${primaryKeyName})" class="btn btn-danger">Delete</a>
</div>
""".trim()
  }
}

<ul class="list-group">
    @for(chapter <- chapters) {
        <li class="list-group-item">

            <a href="@routes.ChapterController.show(chapter.chapterid)" class="btn btn-default">@chapter.chapterid</a>

        </li>
    }
</ul>
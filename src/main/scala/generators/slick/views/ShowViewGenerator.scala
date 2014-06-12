package generators.slick.views

import scala.slick.model.{Column, Table}
import generators.slick.utils.{SlickGeneratorHelpers, ForeignKeyInfo, TableInfo}

class ShowViewGenerator(table : Table, foreignKeyInfo : ForeignKeyInfo) extends ViewHelpers with SlickGeneratorHelpers {

  val mainTableInfo = new TableInfo(table)

  val columns: Seq[Column] = mainTableInfo.columns

  val tableName = mainTableInfo.name

  val foreignKeys = mainTableInfo.foreignKeys

  override val title: String = "Show " + tableName

  val tableRowName = mainTableInfo.tableRowName

  val controllerName = mainTableInfo.controllerName

  val primaryKeyName = mainTableInfo.primaryKeyName

  val childsTables : Seq[TableInfo] = foreignKeyInfo.foreignKeysReferencedTables(table.name).map{ fk =>
    val childTableInfo = new TableInfo(foreignKeyInfo.tablesByName(fk.referencingTable))
    if(childTableInfo.isJunctionTable) {
      val foreignKeyToSecondSide = childTableInfo.foreignKeys.filter(_.referencedTable != table.name).head
      val tableSecondSide = foreignKeyInfo.tablesByName(foreignKeyToSecondSide.referencedTable)
      val tableSecondSideInfo = new TableInfo(tableSecondSide)
      tableSecondSideInfo
    } else childTableInfo
  }

  val childsViewArgs : Seq[(String, String)] = childsTables.map(table => (table.listName, s"List[Tables.${table.tableRowName}]"))

  override val arguments = Seq((tableName, "Tables." + tableRowName)) ++ childsViewArgs

  override def imports: String = ""

  override def bodyCode: String = {
    s"""
<h2>${tableName}</h2>
${fields}

${childs}

${buttons}
""".trim()
  }

  def fields = {
    (columns map {col =>
      if(col.nullable) {
        if(isColumnForeignKey(col)){
          val parentTableInfo = new TableInfo(foreignKeyInfo.tablesByName(foreignKeys.find(_.referencingColumns.head.name.equals(col.name)).get.referencedTable))
          printOptionalForeignKeyField(parentTableInfo.nameCamelCased, parentTableInfo.controllerName, standardColumnName(col.name))
        }
        else {
          s"<p>${col.name} : ${printOptionalField(standardColumnName(col.name))}</p>"
        }
      }
      else {
        if(isColumnForeignKey(col)) {
          val parentTableInfo = new TableInfo(foreignKeyInfo.tablesByName(foreignKeys.find(_.referencingColumns.head.name.equals(col.name)).get.referencedTable))
          printForeignKeyField(parentTableInfo.nameCamelCased, parentTableInfo.controllerName, standardColumnName(col.name))
        }
        else s"<p>${col.name} : @${tableName}.${standardColumnName(col.name)}</p>"
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
          <p>${parentName} : <a href="@routes.${parentControllerName}.show(${foreignKey})" class="btn btn-default">@${foreignKey}</a> </p>
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

  def childs = {
    foreignKeyInfo.foreignKeysReferencedTables(table.name).map{ fk =>
        val childTableInfo = new TableInfo(foreignKeyInfo.tablesByName(fk.referencingTable))
        if(childTableInfo.isJunctionTable) {
          val foreignKeyToSecondSide = childTableInfo.foreignKeys.filter(_.referencedTable != table.name).head
          val tableSecondSide = foreignKeyInfo.tablesByName(foreignKeyToSecondSide.referencedTable)
          val tableSecondSideInfo = new TableInfo(tableSecondSide)
          printJunctionChild(childTableInfo, tableSecondSideInfo)
        } else printChild(childTableInfo)
      }.mkString("\n")
  }

  def printChild(tableInfo : TableInfo) = {

    s"""
<h3>${tableInfo.listName.toUpperCase}</h3>
<ul class="list-group">
    @for(${tableInfo.name} <- ${tableInfo.listName}) {
        <li class="list-group-item">

            <a href="@routes.${tableInfo.controllerName}.show(${tableInfo.name}.${tableInfo.primaryKeyName})" class="btn btn-default">${childRow(tableInfo.name, tableInfo.columns)}</a>

        </li>
    }
</ul>
""".trim()
  }
  
  def printJunctionChild(junctionTableInfo : TableInfo, referencedTableInfo : TableInfo) = {

    val deleteArgs = junctionTableInfo.foreignKeys.map{ fk =>
      if(fk.referencedTable.table.equals(referencedTableInfo.table.name.table)) {
        fk.referencedColumns.map(col => referencedTableInfo.name + "." + standardColumnName(col.name))
      }
      else fk.referencedColumns.map(col => tableName + "." + standardColumnName(col.name))
    }.flatten.mkString(", ")

    s"""
<h3>${referencedTableInfo.listName.toUpperCase}</h3>
<ul class="list-group">
    @for(${referencedTableInfo.name} <- ${referencedTableInfo.listName}) {
        <li class="list-group-item">

            <a href="@routes.${referencedTableInfo.controllerName}.show(${referencedTableInfo.name}.${referencedTableInfo.primaryKeyName})" class="btn btn-default">${childRow(referencedTableInfo.name, referencedTableInfo.columns)}</a>
            <a href="@routes.${controllerName}.delete${junctionTableInfo.nameCamelCased}(${deleteArgs})" class="btn btn-danger">Delete</a>
        </li>
    }
</ul>
""".trim()
  }

  def childRow(rowName : String, columns : Seq[Column]) = {
    columns.take(5).map{ col =>
      s"@${rowName}.${standardColumnName(col.name)}"
    }.mkString(" ")
  }

}
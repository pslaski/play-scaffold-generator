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

  val fieldsAmount = 5

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
<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">${tableName}</h3>

        ${buttons}
    </div>
    <div class="panel-body">
        <div class="show-details">
            ${fields}
        </div>
        <hr>
        ${childs}
    </div>
</div>
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
          printField(col.name, printOptionalFieldValue(standardColumnName(col.name)))
        }
      }
      else {
        if(isColumnForeignKey(col)) {
          val parentTableInfo = new TableInfo(foreignKeyInfo.tablesByName(foreignKeys.find(_.referencingColumns.head.name.equals(col.name)).get.referencedTable))
          printField(parentTableInfo.nameCamelCased, printForeignKeyFieldValue(parentTableInfo.controllerName, standardColumnName(col.name)))
        }
        else printField(col.name, printStandardFieldValue(standardColumnName(col.name)))
      }
    }).mkString("\n")
  }

  def isColumnForeignKey(column : Column) = {
    foreignKeys.exists(_.referencingColumns.head.name.equals(column.name))
  }

  def printField(fieldName : String, fieldValue : String) = {
    s"""
<div class="show-group">
    <span class="col-sm-2 text-right"><strong>${fieldName}:</strong></span>
    <span class="col-sm-10">${fieldValue}</span>
</div>
""".trim()
  }

  def printStandardFieldValue(field : String) = {
    s"@${tableName}.${field}"
  }

  def printOptionalFieldValue(field : String) = {
    s"""
@${tableName}.${field}.map { ${field} =>
          @${field}
        }
""".trim()
  }

  def printOptionalForeignKeyField(parentName: String, parentControllerName : String, foreignKey : String) = {
    s"""
@${tableName}.${foreignKey}.map { ${foreignKey} =>
        <div class="show-group">
            <span class="col-sm-2 text-right"><strong>${parentName}:</strong></span>
            <span class="col-sm-10"><a href="@routes.${parentControllerName}.show(${foreignKey})">@${foreignKey}</a></span>
        </div>
        }
""".trim()
  }

  def printForeignKeyFieldValue(parentControllerName : String, foreignKey : String) = {
    s"""<a href="@routes.${parentControllerName}.show(${tableName}.${foreignKey})">@${tableName}.${foreignKey}</a>"""
  }


  def buttons = {
    s"""
<div class="pull-right btn-group">
  <a href="@routes.${controllerName}.list" class="btn btn-success btn-xs">List</a>
  <a href="@routes.${controllerName}.edit(${tableName}.${primaryKeyName})" class="btn btn-warning btn-xs">Edit</a>
  <a href="@routes.${controllerName}.delete(${tableName}.${primaryKeyName})" class="btn btn-danger btn-xs">Delete</a>
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
<div class="panel panel-info">
    <div class="panel-heading">
        <h3 class="panel-title">${tableInfo.listName.toUpperCase}</h3>
    </div>
    <div class="panel-body">
        <table class="table table-hover table-bordered table-responsive table-middle">
            <thead>
            ${headers(tableInfo.columns)}
            </thead>
            <tbody>
            @for(${tableInfo.name} <- ${tableInfo.listName}) {
            <tr>
                ${childFields(tableInfo.name, tableInfo.columns)}
                <td class="text-center">
                    <a href="@routes.${tableInfo.controllerName}.show(${tableInfo.name}.${tableInfo.primaryKeyName})" class="btn btn-info btn-sm">Show</a>
                </td>
            </tr>
            }
            </tbody>
        </table>
    </div>
</div>
<hr>
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
<div class="panel panel-info">
    <div class="panel-heading">
        <h3 class="panel-title">${referencedTableInfo.listName.toUpperCase}</h3>
    </div>
    <div class="panel-body">
        <table class="table table-hover table-bordered table-responsive table-middle">
            <thead>
            ${headers(referencedTableInfo.columns)}
            </thead>
            <tbody>
            @for(${referencedTableInfo.name} <- ${referencedTableInfo.listName}) {
            <tr>
                ${childFields(referencedTableInfo.name, referencedTableInfo.columns)}
                <td class="text-center">
                    <a href="@routes.${referencedTableInfo.controllerName}.show(${referencedTableInfo.name}.${referencedTableInfo.primaryKeyName})" class="btn btn-info btn-sm">Show</a>
                    <a href="@routes.${controllerName}.delete${junctionTableInfo.nameCamelCased}(${deleteArgs})" class="btn btn-danger btn-sm">Delete</a>
                </td>
            </tr>
            }
            </tbody>
        </table>
    </div>
</div>
<hr>
""".trim()
  }

  def headers(columns : Seq[Column]) = {
    (columns.take(fieldsAmount).map("<th>" + _.name + "</th>") :+ "<th class=\"text-center\">Actions</th>").mkString("\n")
  }

  def childFields(rowName : String, columns : Seq[Column]) = {
    (columns.take(fieldsAmount) map { col =>
      if(col.nullable)  printOptionalListingField(rowName,standardColumnName(col.name))
      else printStandardListingField(rowName, standardColumnName(col.name))
    }).mkString("\n")
  }

  def printStandardListingField(rownName : String, field : String) = {
    s"<td>@${rownName}.${field}</td>"
  }

  def printOptionalListingField(rownName : String, field : String) = {
    s"""
<td>@${rownName}.${field}.map { ${field} =>
      @${field}
    }
</td>
""".trim()
  }

}
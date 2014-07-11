package generators.views

import generators.utils.{TableInfo, ForeignKeyInfo, GeneratorHelpers}

import scala.slick.model.{ForeignKey, Column, Table}

class ShowViewGenerator(table : Table, foreignKeyInfo : ForeignKeyInfo) extends ViewHelpers with GeneratorHelpers {

  val mainTableInfo = new TableInfo(table)

  val columns: Seq[Column] = mainTableInfo.columns

  val tableName = mainTableInfo.nameCamelCasedUncapitalized

  val foreignKeys = mainTableInfo.foreignKeys

  override val title: String = "Show " + tableName

  val tableRowName = mainTableInfo.tableRowName

  val controllerName = mainTableInfo.controllerName

  val primaryKeyColumns: Seq[Column] = mainTableInfo.primaryKeyColumns

  val buttonsArgs = primaryKeyColumns.map(col => tableName + "." + standardColumnName(col.name)).mkString(", ")

  val childsTables : Seq[(TableInfo, ForeignKey)] = foreignKeyInfo.foreignKeysReferencedTables(table.name).map{ fk =>
    val childTableInfo = new TableInfo(foreignKeyInfo.tablesByName(fk.referencingTable))
    if(childTableInfo.isJunctionTable || childTableInfo.isSimpleJunctionTable) {
      val foreignKeyToSecondSide = childTableInfo.foreignKeys.filter(_.referencedTable != table.name).head
      val tableSecondSide = foreignKeyInfo.tablesByName(foreignKeyToSecondSide.referencedTable)
      val tableSecondSideInfo = new TableInfo(tableSecondSide)
      (tableSecondSideInfo, fk)
    } else (childTableInfo, fk)
  }

  val childsViewArgs : Seq[(String, String)] = childsTables.map{ table =>
    val tabInfo = table._1

    val listName = tabInfo.nameCamelCasedUncapitalized + "sBy" + makeColumnsAndString(table._2.referencingColumns)

    (listName, s"List[Tables.${tabInfo.tableRowName}]")
  }

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
  <a href="@routes.${controllerName}.edit(${buttonsArgs})" class="btn btn-warning btn-xs">Edit</a>
  <a href="@routes.${controllerName}.delete(${buttonsArgs})" class="btn btn-danger btn-xs">Delete</a>
</div>
""".trim()
  }

  def childs = {
    foreignKeyInfo.foreignKeysReferencedTables(table.name).map{ fk =>
        val childTableInfo = new TableInfo(foreignKeyInfo.tablesByName(fk.referencingTable))
        if(childTableInfo.isJunctionTable || childTableInfo.isSimpleJunctionTable) {
          val foreignKeyToSecondSide = childTableInfo.foreignKeys.filter(_.referencedTable != table.name).head
          val tableSecondSide = foreignKeyInfo.tablesByName(foreignKeyToSecondSide.referencedTable)
          val tableSecondSideInfo = new TableInfo(tableSecondSide)
          if(childTableInfo.isSimpleJunctionTable) printJunctionChild(childTableInfo, tableSecondSideInfo, fk)
          else printChild(tableSecondSideInfo, fk)
        }
        else printChild(childTableInfo, fk)
      }.mkString("\n")
  }

  def printChild(tableInfo : TableInfo, foreignKey : ForeignKey) = {

    val childTableName = tableInfo.nameCamelCasedUncapitalized

    val childListName = childTableName + "sBy" + makeColumnsAndString(foreignKey.referencingColumns)

    val showArgs = tableInfo.primaryKeyColumns.map(col => childTableName + "." + standardColumnName(col.name)).mkString(", ")

    s"""
<div class="panel panel-info">
    <div class="panel-heading">
        <h3 class="panel-title">${tableInfo.listName.toUpperCase} by ${makeColumnsAndString(foreignKey.referencingColumns)}</h3>
    </div>
    <div class="panel-body">
        <table class="table table-hover table-bordered table-responsive table-middle">
            <thead>
            ${headers(tableInfo.listColumns)}
            </thead>
            <tbody>
            @for(${childTableName} <- ${childListName}) {
            <tr>
                ${childFields(childTableName, tableInfo.listColumns)}
                <td class="text-center">
                    <a href="@routes.${tableInfo.controllerName}.show(${showArgs})" class="btn btn-info btn-sm">Show</a>
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
  
  def printJunctionChild(junctionTableInfo : TableInfo, referencedTableInfo : TableInfo, foreignKey : ForeignKey) = {

    val referencedTableName = referencedTableInfo.nameCamelCasedUncapitalized

    val childListName = referencedTableName + "sBy" + makeColumnsAndString(foreignKey.referencingColumns)

    val showArgs = referencedTableInfo.primaryKeyColumns.map(col => referencedTableName + "." + standardColumnName(col.name)).mkString(", ")

    val deleteArgs = junctionTableInfo.foreignKeys.map{ fk =>
      if(fk.referencedTable.table.equals(referencedTableInfo.table.name.table)) {
        fk.referencedColumns.map(col => referencedTableName + "." + standardColumnName(col.name))
      }
      else fk.referencedColumns.map(col => tableName + "." + standardColumnName(col.name))
    }.flatten.mkString(", ")

    s"""
<div class="panel panel-info">
    <div class="panel-heading">
        <h3 class="panel-title">${referencedTableInfo.listName.toUpperCase} by ${makeColumnsAndString(foreignKey.referencingColumns)}</h3>
    </div>
    <div class="panel-body">
        <table class="table table-hover table-bordered table-responsive table-middle">
            <thead>
            ${headers(referencedTableInfo.listColumns)}
            </thead>
            <tbody>
            @for(${referencedTableName} <- ${childListName}) {
            <tr>
                ${childFields(referencedTableName, referencedTableInfo.listColumns)}
                <td class="text-center">
                    <a href="@routes.${referencedTableInfo.controllerName}.show(${showArgs})" class="btn btn-info btn-sm">Show</a>
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
    (columns.map("<th>" + _.name + "</th>") :+ "<th class=\"text-center\">Actions</th>").mkString("\n")
  }

  def childFields(rowName : String, columns : Seq[Column]) = {
    (columns map { col =>
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
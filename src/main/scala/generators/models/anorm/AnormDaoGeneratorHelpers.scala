package generators.models.anorm

import generators.utils.{TableInfo, GeneratorHelpers}

import scala.slick.model.{Column, ForeignKey}

trait AnormDaoGeneratorHelpers extends GeneratorHelpers{

  val rowName : String

  val tableName : String

  val tableRowName : String

  val tableParserName : String

  val primaryKeyColumns : Seq[Column]

  val allColumns : Seq[Column]

  val fieldsForSimpleName : Seq[String]
  
  def saveReturnIdMethodCode(autoIncColumn : Column) = {

    val newObjName = "new" + rowName.capitalize

    val columnsWithoutAutoInc = allColumns.filterNot(isAutoIncColumn(_))

    val columnNames = makeColumnNamesList(columnsWithoutAutoInc)

    val values = makeValuesList(columnsWithoutAutoInc)

    val symbols = makeArrowAssocWithObjectName(rowName, columnsWithoutAutoInc)
    
    s"""
def save(${rowName}: ${tableRowName}) : ${tableRowName} = {

  val autoIncValue = SQL(
    \"\"\"INSERT INTO "${tableName}" (${columnNames})
       VALUES (${values})\"\"\")
    .on(${symbols})
    .executeInsert(scalar[${autoIncColumn.tpe}].single)

  val ${newObjName} = ${rowName}.copy(${standardColumnName(autoIncColumn.name)} = autoIncValue)
  ${newObjName}
}
""".trim()
  }

  def saveSimpleMethodCode = {

    val columnNames = makeColumnNamesList(allColumns)

    val values = makeValuesList(allColumns)

    val symbols = makeArrowAssocWithObjectName(rowName, allColumns)

    s"""
def save(${rowName}: ${tableRowName}) : ${tableRowName} = {
  SQL(\"\"\"INSERT INTO "${tableName}" (${columnNames})
          VALUES (${values})\"\"\")
    .on(${symbols})
    .executeUpdate()
  ${rowName}
}""".trim()
  }
  
  def findAllMethodCode = {
    s"""
def findAll : List[${tableRowName}] = {
  SQL(\"\"\"SELECT * FROM "${tableName}" \"\"\")
    .as(${tableParserName} *)
}
""".trim()
  }
  
  def findByPrimaryKeyMethodCode = {

    val methodArgs = makeArgsWithTypes(primaryKeyColumns)

    val whereClause = primaryKeyColumns.map(makeSQLColumnAssigning(_)).mkString(" AND ")

    val symbols = makeArrowAssoc(primaryKeyColumns)

    s"""
def findByPrimaryKey(${methodArgs}) : Option[${tableRowName}] = {
  SQL(\"\"\"SELECT * FROM "${tableName}" WHERE ${whereClause} \"\"\")
    .on(${symbols})
    .as(${tableParserName}.singleOpt)
}
""".trim()
  }
  
  def deleteMethodCode(childData : Seq[(TableInfo, ForeignKey)]) = {

    val methodArgs = makeArgsWithTypes(primaryKeyColumns)

    val whereClause = primaryKeyColumns.map(makeSQLColumnAssigning(_)).mkString(" AND ")

    val symbols = makeArrowAssoc(primaryKeyColumns)

    val queryArgs = makeArgsWithoutTypes(primaryKeyColumns)

    val childsCode = {
      if(childData.nonEmpty){

s"""
  val objOption = findByPrimaryKey(${queryArgs})

  objOption match {
    case Some(obj) => {
      ${childData.map(child => deleteChilds(child._1, child._2)).mkString("\n\t\t\t")}
    }
    case None => "Not finded"
  }

""".trim()
      } else ""
    }

    s"""
def delete(${methodArgs}) = {
  ${childsCode}
  val rowsDeleted = SQL(\"\"\"DELETE FROM "${tableName}" WHERE ${whereClause} \"\"\")
    .on(${symbols})
    .executeUpdate()
  rowsDeleted
}
""".trim()
  }

  def deleteChilds(childTabInfo : TableInfo, fk : ForeignKey) = {

    val deleteQuery = makeDeleteByMethodName(fk.referencingColumns)

    val queryArgs = makeArgsWithObjectWithoutTypes("obj", fk.referencedColumns)

    s"${childTabInfo.daoObjectName}.${deleteQuery}(${queryArgs})"
  }

  def deleteJunctionMethodCode(foreignKeys : Seq[ForeignKey]) = {

    val idColumns = foreignKeys.map{ fk =>
      makeArgsWithTypes(fk.referencingColumns)
    }.mkString(", ")

    val whereClause = foreignKeys.map{ fk =>
      fk.referencingColumns.map(makeSQLColumnAssigning(_))
    }.flatten.mkString(" AND ")

    val symbols = foreignKeys.map{ fk =>
      makeArrowAssoc(fk.referencingColumns)
    }.mkString(",\n\t\t\t\t")

    s"""
def delete(${idColumns}) = {
  val rowsDeleted = SQL(\"\"\"DELETE FROM "${tableName}" WHERE ${whereClause} \"\"\")
    .on(${symbols})
    .executeUpdate()
  rowsDeleted
}""".trim()
  }
  
  def updateMethodCode = {

    val setClause = allColumns.filterNot(isAutoIncColumn(_)).map(makeSQLColumnAssigning(_)).mkString(",\n\t\t\t\t\t\t")

    val whereClause = primaryKeyColumns.map(makeSQLColumnAssigning(_)).mkString(" AND ")

    val symbols = makeArrowAssocWithObjectName("updatedRow", allColumns)

    s"""
def update(updatedRow: ${tableRowName}) = {
  val rowsUpdated = SQL(
    \"\"\"UPDATE "${tableName}"
        SET ${setClause}
        WHERE ${whereClause} \"\"\")
    .on(${symbols})
    .executeUpdate()
  rowsUpdated
}
""".trim()
  }

  def findByMethodCode(columns : Seq[Column]) = {

    val args = makeArgsWithTypes(columns)

    val methodName = makeFindByMethodName(columns)

    val whereClause = columns.map(makeSQLColumnAssigning(_)).mkString(" AND ")

    val symbols = makeArrowAssoc(columns)

  s"""
def ${methodName}(${args}) : List[${tableRowName}] = {
  SQL(\"\"\"SELECT * FROM "${tableName}" WHERE ${whereClause} \"\"\")
    .on(${symbols})
    .as(${tableParserName} *)
}
""".trim()
  }

  def findByUniqueMethodCode(columns : Seq[Column]) = {

    val args = makeArgsWithTypes(columns)

    val methodName = makeFindByMethodName(columns)

    val whereClause = columns.map(makeSQLColumnAssigning(_)).mkString(" AND ")

    val symbols = makeArrowAssoc(columns)

  s"""
def ${methodName}(${args}) : Option[${tableRowName}] = {
  SQL(\"\"\"SELECT * FROM "${tableName}" WHERE ${whereClause} \"\"\")
    .on(${symbols})
    .as(${tableParserName}.singleOpt)
}
""".trim()
  }

  def deleteByMethodCode(columns : Seq[Column]) = {

    val args = makeArgsWithTypes(columns)

    val methodName = makeDeleteByMethodName(columns)

    val whereClause = columns.map(makeSQLColumnAssigning(_)).mkString(" AND ")

    val symbols = makeArrowAssoc(columns)

  s"""
def ${methodName}(${args}) = {
  SQL(\"\"\"DELETE FROM "${tableName}" WHERE ${whereClause} \"\"\")
    .on(${symbols})
    .executeUpdate()
}
""".trim()
  }

  def findByJunctionTableMethodsCode(junctionTableInfo : TableInfo, foreignKeyToFirstSide : ForeignKey, foreignKeyToSecondSide : ForeignKey) = {

    val secondSideReferencingColumns = foreignKeyToSecondSide.referencingColumns

    val findByMethodName = makeFindByMethodName(secondSideReferencingColumns)

    val findByMethodArgs = makeArgsWithTypes(secondSideReferencingColumns)

    val junctionTableName = junctionTableInfo.table.name.table

    val filteringColumns = secondSideReferencingColumns.map{ column =>
      "b." + makeSQLColumnAssigning(column)
    }.mkString(" AND ")

    val joiningColumns = {
      ((foreignKeyToFirstSide.referencedColumns.map(_.name) zip foreignKeyToFirstSide.referencingColumns.map(_.name)).map{
        case (lcol,rcol) => "a.\"" + lcol + "\"" + " = " + "b.\"" + rcol + "\""
      }.mkString(" AND "))
    }

    val whereClause = filteringColumns + " AND " + joiningColumns

    val symbols = makeArrowAssoc(secondSideReferencingColumns)

  s"""
def ${findByMethodName}(${findByMethodArgs}) : List[${tableRowName}] = {
  SQL(\"\"\"SELECT a.* FROM "${tableName}" a, "${junctionTableName}" b WHERE ${whereClause} \"\"\")
    .on(${symbols})
    .as(${tableParserName} *)
}
""".trim()
  }

  def formOptionsMethodCode(colName : String) = {

    val id = standardColumnName(colName)

    val byName = id.capitalize

    s"""
def formOptionsBy${byName} : Seq[(String, String)] = {
  findAll.map{ row =>
    (row.${id}.toString, ${fieldsForSimpleName.map("row." + _).mkString(" + \" \" + ")})
  }
}""".trim()
  }
  
}

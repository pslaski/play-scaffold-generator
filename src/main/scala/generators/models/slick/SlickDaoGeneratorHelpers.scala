package generators.models.slick

import generators.utils.{TableInfo, GeneratorHelpers}

import scala.slick.model.{Column, ForeignKey}

trait SlickDaoGeneratorHelpers extends GeneratorHelpers{

  val rowName : String

  val rowNameCamelCased : String

  val tableRowName : String

  val primaryKeyName : String

  val primaryKeyType : String

  val primaryKeyColumns : Seq[Column]

  val queryObjectName : String

  val fieldsForSimpleName : Seq[String]
  
  def saveReturnIdMethodCode(autoIncId : String) = {
    
    s"""
def save(${rowName}: ${tableRowName}) : ${tableRowName} = {
  ${queryObjectName} returning ${queryObjectName}.map(_.${autoIncId}) into((row, id) => row.copy(${autoIncId} = id)) insert(${rowName})
}""".trim()
  }

  def saveSimpleMethodCode = {

    s"""
def save(${rowName}: ${tableRowName}) : ${tableRowName} = {
  ${queryObjectName} insert(${rowName})
  ${rowName}
}""".trim()
  }
  
  def findAllMethodCode = {
    s"""
def findAll : List[${tableRowName}] = {
  ${queryObjectName}.list
}""".trim()
  }
  
  def findByIdMethodCode = {
    s"""
def findById(${makeArgsWithTypes(primaryKeyColumns)}) : Option[${tableRowName}] = {
  val queryFindById = ${findByCode(primaryKeyColumns)}

  queryFindById.firstOption
}""".trim()
  }
  
  def deleteMethodCode(childData : Seq[(TableInfo, ForeignKey)]) = {

    val childsCode = {
      if(childData.nonEmpty){
s"""
   val objOption = queryFindById.firstOption

   objOption match {
     case Some(obj) => {
       ${childData.map(child => deleteChilds(child._1, child._2)).mkString("\n")}
     }
     case None => "Not finded"
   }
 """.trim()
      } else ""
    }

    s"""
def delete(${makeArgsWithTypes(primaryKeyColumns)}) = {

  val queryFindById = ${findByCode(primaryKeyColumns)}

  ${childsCode}

  queryFindById.delete
}""".trim()
  }

  def deleteChilds(childTabInfo : TableInfo, fk : ForeignKey) = {
    val referencingColumns = makeColumnsAndString(fk.referencingColumns)

    s"${childTabInfo.daoObjectName}.delete${childTabInfo.nameCamelCased + "s"}By${referencingColumns}For${queryObjectName}(obj)"
  }

  def findByCode(columns : Seq[Column]) = {

    val findingColumns = columns.map { column =>
      val colName = standardColumnName(column.name)
      s"""row.${colName} === ${colName}"""
    }.mkString(" && ")

    s"""
for {
    row <- ${queryObjectName} if ${findingColumns}
  } yield row""".trim()
  }

  def deleteJunctionMethodCode(foreignKeys : Seq[ForeignKey]) = {

    val idColumns = foreignKeys.map{ fk =>
      fk.referencingColumns.map( col => standardColumnName(col.name) + " : " + col.tpe)
    }.flatten.mkString(", ")

    val findingColumns = foreignKeys.map{ fk =>
      fk.referencingColumns.map(col => "row." + standardColumnName(col.name) + " === " + standardColumnName(col.name))
    }.flatten.mkString(" && ")

    s"""
def delete(${idColumns}) = {

  val queryFindById = for {
      row <- ${queryObjectName} if ${findingColumns}
    } yield row

  queryFindById.delete
}""".trim()
  }
  
  def updateMethodCode = {

    val findingColumns = primaryKeyColumns.map { column =>
      val colName = standardColumnName(column.name)
      s"""row.${colName} === updatedRow.${colName}"""
    }.mkString(" && ")

    s"""
def update(updatedRow: ${tableRowName}) = {
  val queryFindById = for {
    row <- ${queryObjectName} if ${findingColumns}
  } yield row

  queryFindById.update(updatedRow)
}""".trim()
  }

  def findByForeignKeyQueryMethodCode(referencedTableInfo : TableInfo, foreignKey : ForeignKey) = {

    val referencedRow = referencedTableInfo.nameCamelCasedUncapitalized

    val referencingColumns = makeColumnsAndString(foreignKey.referencingColumns)

    val methodName = s"${rowNameCamelCased.uncapitalize+"s"}By${referencingColumns}For${referencedTableInfo.nameCamelCased}Query"

    val joiningColumns = {
      "row => " + ((foreignKey.referencingColumns.map(_.name) zip foreignKey.referencedColumns.map(_.name)).map{
        case (lcol,rcol) => "row." + standardColumnName(lcol) + " === " + referencedRow + "." + standardColumnName(rcol)
      }.mkString(" && "))
    }

  s"""
def ${methodName}(${referencedRow} : ${referencedTableInfo.tableRowName}) = {
  ${queryObjectName}.filter(${joiningColumns})
}
""".trim()
  }

  def findByForeignKeyMethodCode(referencedTableInfo : TableInfo, foreignKey : ForeignKey) = {

    val referencedRow = referencedTableInfo.nameCamelCasedUncapitalized

    val referencingColumns = makeColumnsAndString(foreignKey.referencingColumns)

    val methodName = s"${rowNameCamelCased.uncapitalize+"s"}By${referencingColumns}For${referencedTableInfo.nameCamelCased}"

    val queryName = methodName + "Query"

  s"""
def ${methodName}(${referencedRow} : ${referencedTableInfo.tableRowName}) : List[${tableRowName}] = {
  ${queryName}(${referencedRow}).list
}
""".trim()
  }

  def deleteByForeignKeyMethodCode(referencedTableInfo : TableInfo, foreignKey : ForeignKey) = {

    val referencedRow = referencedTableInfo.nameCamelCasedUncapitalized

    val referencingColumns = makeColumnsAndString(foreignKey.referencingColumns)

    val methodName = s"delete${rowNameCamelCased +"s"}By${referencingColumns}For${referencedTableInfo.nameCamelCased}"

    val queryName = s"${rowNameCamelCased.uncapitalize+"s"}By${referencingColumns}For${referencedTableInfo.nameCamelCased}Query"

    s"""
def ${methodName}(${referencedRow} : ${referencedTableInfo.tableRowName}) = {
  ${queryName}(${referencedRow}).delete
}
""".trim()
  }

  def findByJunctionTableMethodCode(junctionTableInfo : TableInfo,referencedTableInfo : TableInfo, foreignKeyToFirstSide : ForeignKey, foreignKeyToSecondSide : ForeignKey) = {

    val referencedRow = referencedTableInfo.nameCamelCasedUncapitalized

    val referencingColumns = makeColumnsAndString(foreignKeyToSecondSide.referencingColumns)

    val methodName = s"${rowNameCamelCased.uncapitalize+"s"}For${referencedTableInfo.nameCamelCased}"

    val junctionRow = junctionTableInfo.name.uncapitalize

    val joiningColumns = {
      "row => " + ((foreignKeyToFirstSide.referencedColumns.map(_.name) zip foreignKeyToFirstSide.referencingColumns.map(_.name)).map{
        case (lcol,rcol) => "row." + standardColumnName(lcol) + " === " + junctionRow + "." + standardColumnName(rcol)
      }.mkString(" && "))
    }

    val findJunctionQueryName = s"${junctionTableInfo.nameCamelCasedUncapitalized+"s"}By${referencingColumns}For${referencedTableInfo.nameCamelCased}Query"

    val resultListName = rowNameCamelCased.uncapitalize+"s"

  s"""
def ${methodName}(${referencedRow} : ${referencedTableInfo.tableRowName}) : List[${tableRowName}] = {
  val query = for {
    ${junctionRow} <- ${junctionTableInfo.daoObjectName}.${findJunctionQueryName}(${referencedRow})
    ${resultListName} <- ${queryObjectName}.filter(${joiningColumns})
  } yield ${resultListName}

  query.list
}
""".trim()
  }

  def formOptionsMethodCode(colName : String) = {

    s"""
def formOptionsBy${colName.toCamelCase} : Seq[(String, String)] = {
  ${queryObjectName}.list.map{ row =>
    (row.${colName}.toString, ${fieldsForSimpleName.map("row." + _).mkString(" + \" \" + ")})
  }
}""".trim()
  }
  
}

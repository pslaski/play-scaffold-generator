package generators.models.squeryl

import generators.utils.{TableInfo, GeneratorHelpers}

import scala.slick.model.{Column, ForeignKey}

trait SquerylDaoGeneratorHelpers extends GeneratorHelpers{

  val rowName : String

  val listName : String

  val tableRowName : String

  val primaryKeyColumns : Seq[Column]

/*  val primaryKeyName : String

  val primaryKeyType : String*/

  val queryObjectName : String

  val fieldsForSimpleName : Seq[String]
  
  def saveMethodCode = {
    
    s"""
def save(${rowName}: ${tableRowName}) : ${tableRowName} = {
  inTransaction(${queryObjectName}.insert(${rowName}))
}""".trim()
  }

/*  def saveJunctionMethodCode = {

    s"""
def save(${rowName}: ${tableRowName}) = {
  inTransaction(${queryObjectName}.insert(${rowName}))
}""".trim()
  }*/
  
  def findAllMethodCode = {
    s"""
def findAll : List[${tableRowName}] = {
  inTransaction(${queryObjectName}.toList)
}""".trim()
  }

  def findByPrimaryKeyMethodCode = {

    val methodArgs = makeArgsWithTypes(primaryKeyColumns)

    val queryArgs = makeCompositeKey(primaryKeyColumns)

    s"""
def findByPrimaryKey(${methodArgs}) : Option[${tableRowName}] = {
  inTransaction(${queryObjectName}.lookup(${queryArgs}))
}""".trim()
  }
  
/*  def findByIdMethodCode = {
    s"""
def findById(id : ${primaryKeyType}) : Option[${tableRowName}] = {
  inTransaction(${queryObjectName}.lookup(id))
}""".trim()
  }*/
  
  def deleteMethodCode(childData : Seq[(TableInfo, ForeignKey)]) = {

    val methodArgs = makeArgsWithTypes(primaryKeyColumns)

    val queryArgs = makeCompositeKey(primaryKeyColumns)

    val deleteChilds = childData.map(data => deleteChild(data._1, data._2)).mkString("\n")

    s"""
def delete(${methodArgs}) = {
  ${deleteChilds}
  inTransaction(${queryObjectName}.delete(${queryArgs}))
}""".trim()
  }

  def deleteChild(childTabInfo : TableInfo, fk : ForeignKey) : String = {

    val deleteQuery = makeDeleteByMethodName(fk.referencingColumns)

    val queryArgs = makeArgsWithoutTypes(fk.referencedColumns)

    s"${childTabInfo.daoObjectName}.${deleteQuery}(${queryArgs})"
  }

/*  def deleteJunctionMethodCode(foreignKeys : Seq[ForeignKey]) = {

    val idColumns = foreignKeys.map{ fk =>
      fk.referencingColumns.map( col => standardColumnName(col.name) + " : " + col.tpe)
    }.flatten.mkString(", ")

    val findingColumns = foreignKeys.map{ fk =>
      fk.referencingColumns.map(col => standardColumnName(col.name))
    }.flatten.mkString(", ")

    s"""
def delete(${idColumns}) = {
  inTransaction(${queryObjectName}.delete(compositeKey(${findingColumns})))
}""".trim()
  }*/

  def deleteSimpleJunctionMethodCode(foreignKeys : Seq[ForeignKey]) = {

    val args = foreignKeys.map{ fk =>
      makeArgsWithTypes(fk.referencingColumns)
    }.mkString(", ")

    val whereCondition = foreignKeys.map{ fk =>
      makeSquerylRowComparing(fk.referencingColumns)
    }.mkString(" and ")

  s"""
def delete(${args}) = {
  inTransaction{
    ${queryObjectName}.deleteWhere(row => ${whereCondition})
  }
}
""".trim()
  }
  
  def updateMethodCode = {
    s"""
def update(updatedRow: ${tableRowName}) = {
  inTransaction(${queryObjectName}.update(updatedRow))
}""".trim()
  }

  def findByQueryMethodCode(columns : Seq[Column]) = {

    val args = makeArgsWithTypes(columns)

    val methodName = makeFindByQueryMethodName(columns)

    val whereCondition = makeSquerylRowComparing(columns)

  s"""
def ${methodName}(${args}) = {
  ${queryObjectName}.where(row => ${whereCondition})
}
""".trim()
  }

  def findByMethodCode(columns : Seq[Column]) = {

    val args = makeArgsWithTypes(columns)

    val methodName = makeFindByMethodName(columns)

    val queryName = makeFindByQueryMethodName(columns)

    val queryArgs = makeArgsWithoutTypes(columns)

  s"""
def ${methodName}(${args}) : List[${tableRowName}] = {
  inTransaction{
    ${queryName}(${queryArgs}).toList
  }
}
""".trim()
  }

  def findByUniqueMethodCode(columns : Seq[Column]) = {

    val args = makeArgsWithTypes(columns)

    val methodName = makeFindByMethodName(columns)

    val whereCondition = makeSquerylRowComparing(columns)

  s"""
def ${methodName}(${args}) : Option[${tableRowName}] = {
  inTransaction{
    ${queryObjectName}.where(row => ${whereCondition}).headOption
  }
}
""".trim()
  }

  def deleteByMethodCode(columns : Seq[Column]) = {

    val args = makeArgsWithTypes(columns)

    val methodName = makeDeleteByMethodName(columns)

    val whereCondition = makeSquerylRowComparing(columns)

  s"""
def ${methodName}(${args}) = {
  inTransaction{
    ${queryObjectName}.deleteWhere(row => ${whereCondition})
  }
}
""".trim()
  }

  def findByJunctionTableMethodsCode(junctionTableInfo : TableInfo, foreignKeyToFirstSide : ForeignKey, foreignKeyToSecondSide : ForeignKey) = {

    val secondSideReferencingColumns = foreignKeyToSecondSide.referencingColumns

    val findByMethodName = makeFindByMethodName(secondSideReferencingColumns)

    val queryArgs = makeArgsWithTypes(secondSideReferencingColumns)

    val queryName = makeFindByQueryMethodName(secondSideReferencingColumns)

    val findJunctionArgs = makeArgsWithoutTypes(secondSideReferencingColumns)

    val junctionRow = junctionTableInfo.listName

    val joiningColumns = {
      ((foreignKeyToFirstSide.referencedColumns.map(_.name) zip foreignKeyToFirstSide.referencingColumns.map(_.name)).map{
        case (lcol,rcol) => listName + "." + standardColumnName(lcol) + " === " + junctionRow + "." + standardColumnName(rcol)
      }.mkString(" and "))
    }

  s"""
def ${findByMethodName}(${queryArgs}) : List[${tableRowName}] = {

  val junctionQuery = ${junctionTableInfo.daoObjectName}.${queryName}(${findJunctionArgs})

  inTransaction{
    from(${queryObjectName}, junctionQuery)((${listName}, ${junctionRow}) =>
      where(${joiningColumns})
      select(${listName})
    ).toList
  }
}
""".trim()
  }

/*  def findByForeignKeyMethodCode(referencedTableInfo : TableInfo) = {

    val referencedRow = referencedTableInfo.nameCamelCasedUncapitalized

    val methodName = s"${listName}For${referencedTableInfo.nameCamelCased}"

  s"""
def ${methodName}(${referencedRow} : ${referencedTableInfo.tableRowName}) : List[${tableRowName}] = {
  inTransaction(${referencedRow}.${listName}.toList)
}
""".trim()
  }*/


  def formOptionsMethodCode(colName : String) = {

    val id = standardColumnName(colName)

    val byName = id.capitalize

    s"""
def formOptionsBy${byName} : Seq[(String, String)] = {
  inTransaction{
    ${queryObjectName}.map{ row =>
      (row.${id}.toString, ${fieldsForSimpleName.map("row." + _).mkString(" + \" \" + ")})
    }.toSeq
  }
}""".trim()
  }
  
}

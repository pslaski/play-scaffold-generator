package generators.models.slick

import generators.utils.{TableInfo, GeneratorHelpers}

import scala.slick.model.{Column, ForeignKey}

trait SlickDaoGeneratorHelpers extends GeneratorHelpers{

  val rowName : String

  val rowNameCamelCased : String

  val tableRowName : String

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
  
  def findByPrimaryKeyMethodCode = {

    val methodArgs = makeArgsWithTypes(primaryKeyColumns)

    val queryName = makeFindByQueryCompiledMethodName(primaryKeyColumns)

    val queryArgs = makeArgsWithoutTypes(primaryKeyColumns)

    s"""
def findByPrimaryKey(${methodArgs}) : Option[${tableRowName}] = {
  ${queryName}(${queryArgs}).firstOption
}""".trim()
  }
  
  def deleteMethodCode(childData : Seq[(TableInfo, ForeignKey)]) = {

    val methodArgs = makeArgsWithTypes(primaryKeyColumns)

    val queryArgs = makeArgsWithoutTypes(primaryKeyColumns)

    val findQuery = makeFindByQueryCompiledMethodName(primaryKeyColumns)

    val childsCode = {
      if(childData.nonEmpty){

s"""
   val objOption = findByPrimaryKey(${queryArgs})

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
def delete(${methodArgs}) = {
  ${childsCode}
  ${findQuery}(${queryArgs}).delete
}""".trim()
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

    val findingColumns = foreignKeys.map{ fk =>
      makeSlickRowComparing(fk.referencingColumns)
    }.mkString(" && ")

    s"""
def delete(${idColumns}) = {

  val queryFindById = for {
      row <- ${queryObjectName} if ${findingColumns}
    } yield row

  queryFindById.delete
}""".trim()
  }
  
  def updateMethodCode = {

    val queryName = makeFindByQueryCompiledMethodName(primaryKeyColumns)

    val queryArgs = makeArgsWithObjectWithoutTypes("updatedRow", primaryKeyColumns)

    s"""
def update(updatedRow: ${tableRowName}) = {
  ${queryName}(${queryArgs}).update(updatedRow)
}""".trim()
  }

  def findByQueryMethodCode(columns : Seq[Column]) = {

    val args = makeArgsWithColumnTypes(columns)

    val rowComparingArgs = makeSlickRowComparing(columns)

    val methodName = makeFindByQueryMethodName(columns)

    val compiledName = makeFindByQueryCompiledMethodName(columns)

  s"""
def ${methodName}(${args}) = {
  ${queryObjectName}.filter(row => ${rowComparingArgs})
}

val ${compiledName} = Compiled(${methodName} _)
""".trim()
  }

  def findByMethodCode(columns : Seq[Column]) = {

    val args = makeArgsWithTypes(columns)

    val methodName = makeFindByMethodName(columns)

    val compiledName = makeFindByQueryCompiledMethodName(columns)

    val compiledArgs = makeArgsWithoutTypes(columns)

  s"""
def ${methodName}(${args}) : List[${tableRowName}] = {
  ${compiledName}(${compiledArgs}).list
}
""".trim()
  }

  def findByUniqueMethodCode(columns : Seq[Column]) = {

    val args = makeArgsWithTypes(columns)

    val methodName = makeFindByMethodName(columns)

    val compiledName = makeFindByQueryCompiledMethodName(columns)

    val compiledArgs = makeArgsWithoutTypes(columns)

  s"""
def ${methodName}(${args}) : Option[${tableRowName}] = {
  ${compiledName}(${compiledArgs}).firstOption
}
""".trim()
  }

  def deleteByMethodCode(columns : Seq[Column]) = {

    val args = makeArgsWithTypes(columns)

    val methodName = makeDeleteByMethodName(columns)

    val compiledName = makeFindByQueryCompiledMethodName(columns)

    val compiledArgs = makeArgsWithoutTypes(columns)

  s"""
def ${methodName}(${args}) = {
  ${compiledName}(${compiledArgs}).delete
}
""".trim()
  }

  def findByJunctionTableMethodsCode(junctionTableInfo : TableInfo, foreignKeyToFirstSide : ForeignKey, foreignKeyToSecondSide : ForeignKey) = {

    val secondSideReferencingColumns = foreignKeyToSecondSide.referencingColumns

    val queryName = makeFindByQueryMethodName(secondSideReferencingColumns)

    val queryArgs = makeArgsWithColumnTypes(secondSideReferencingColumns)

    val junctionRow = junctionTableInfo.name.uncapitalize

    val joiningColumns = {
      "row => " + ((foreignKeyToFirstSide.referencedColumns.map(_.name) zip foreignKeyToFirstSide.referencingColumns.map(_.name)).map{
        case (lcol,rcol) => "row." + standardColumnName(lcol) + " === " + junctionRow + "." + standardColumnName(rcol)
      }.mkString(" && "))
    }

    val findJunctionArgs = makeArgsWithoutTypes(secondSideReferencingColumns)

    val resultListName = rowNameCamelCased.uncapitalize+"s"

    val compiledName = makeFindByQueryCompiledMethodName(secondSideReferencingColumns)

    val findByMethodName = makeFindByMethodName(secondSideReferencingColumns)

    val findByMethodArgs = makeArgsWithTypes(secondSideReferencingColumns)

  s"""
def ${queryName}(${queryArgs}) = {
  for {
    ${junctionRow} <- ${junctionTableInfo.daoObjectName}.${queryName}(${findJunctionArgs})
    ${resultListName} <- ${queryObjectName}.filter(${joiningColumns})
  } yield ${resultListName}
}

val ${compiledName} = Compiled(${queryName} _)

def ${findByMethodName}(${findByMethodArgs}) : List[${tableRowName}] = {
  ${compiledName}(${findJunctionArgs}).list
}
""".trim()
  }

  def formOptionsMethodCode(colName : String) = {

    val id = standardColumnName(colName)

    val byName = id.capitalize

    s"""
def formOptionsBy${byName} : Seq[(String, String)] = {
  ${queryObjectName}.list.map{ row =>
    (row.${id}.toString, ${fieldsForSimpleName.map("row." + _).mkString(" + \" \" + ")})
  }
}""".trim()
  }
  
}

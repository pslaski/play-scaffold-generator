package generators.slick.models

import generators.slick.utils.{SlickGeneratorHelpers, TableInfo}
import scala.slick.model.ForeignKey

trait DaoGeneratorHelpers extends SlickGeneratorHelpers{

  val rowName : String

  val rowNameCamelCased : String

  val tableRowName : String

  val primaryKeyName : String

  val primaryKeyType : String

  val queryObjectName : String

  val fieldsForSimpleName : Seq[String]
  
  def saveMethodCode = {
    
    s"""
def save(${rowName}: ${tableRowName}) : ${primaryKeyType} = {
  ${queryObjectName} returning ${queryObjectName}.map(_.${primaryKeyName}) insert(${rowName})
}""".trim()
  }

  def saveJunctionMethodCode = {

    s"""
def save(${rowName}: ${tableRowName}) = {
  ${queryObjectName} insert(${rowName})
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
def findById(${primaryKeyName}: ${primaryKeyType}) : Option[${tableRowName}] = {
  val queryFindById = ${findByCode(primaryKeyName)}

  queryFindById.firstOption
}""".trim()
  }
  
  def deleteMethodCode(childData : Seq[(String,String)]) = {

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
def delete(${primaryKeyName}: ${primaryKeyType}) = {

  val queryFindById = ${findByCode(primaryKeyName)}

  ${childsCode}

  queryFindById.delete
}""".trim()
  }

  def deleteChilds(childDao : String, childName : String) = {
    s"${childDao}.delete${childName + "s"}For${queryObjectName}(obj)"
  }

  def findByCode(fieldName : String) = {
    s"""
for {
    row <- ${queryObjectName} if row.${fieldName} === ${fieldName}
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
    s"""
def update(updatedRow: ${tableRowName}) = {
  val queryFindById = for {
    row <- ${queryObjectName} if row.${primaryKeyName} === updatedRow.${primaryKeyName}
  } yield row

  queryFindById.update(updatedRow)
}""".trim()
  }

  def findByForeignKeyQueryMethodCode(referencedTableInfo : TableInfo, foreignKey : ForeignKey) = {

    val referencedRow = referencedTableInfo.nameCamelCasedUncapitalized

    val methodName = s"${rowNameCamelCased.uncapitalize+"s"}For${referencedTableInfo.nameCamelCased}Query"

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

  def findByForeignKeyMethodCode(referencedTableInfo : TableInfo) = {

    val referencedRow = referencedTableInfo.nameCamelCasedUncapitalized

    val methodName = s"${rowNameCamelCased.uncapitalize+"s"}For${referencedTableInfo.nameCamelCased}"

    val queryName = methodName + "Query"

  s"""
def ${methodName}(${referencedRow} : ${referencedTableInfo.tableRowName}) : List[${tableRowName}] = {
  ${queryName}(${referencedRow}).list
}
""".trim()
  }

  def deleteByForeignKeyMethodCode(referencedTableInfo : TableInfo) = {

    val referencedRow = referencedTableInfo.nameCamelCasedUncapitalized

    val methodName = s"delete${rowNameCamelCased +"s"}For${referencedTableInfo.nameCamelCased}"

    val queryName = s"${rowNameCamelCased.uncapitalize+"s"}For${referencedTableInfo.nameCamelCased}Query"

    s"""
def ${methodName}(${referencedRow} : ${referencedTableInfo.tableRowName}) = {
  ${queryName}(${referencedRow}).delete
}
""".trim()
  }

  def findByJunctionTableMethodCode(junctionTableInfo : TableInfo,referencedTableInfo : TableInfo, foreignKey : ForeignKey) = {

    val referencedRow = referencedTableInfo.nameCamelCasedUncapitalized

    val methodName = s"${rowNameCamelCased.uncapitalize+"s"}For${referencedTableInfo.nameCamelCased}"

    val junctionRow = junctionTableInfo.name

    val joiningColumns = {
      "row => " + ((foreignKey.referencedColumns.map(_.name) zip foreignKey.referencingColumns.map(_.name)).map{
        case (lcol,rcol) => "row." + standardColumnName(lcol) + " === " + junctionRow + "." + standardColumnName(rcol)
      }.mkString(" && "))
    }

    val findJunctionQueryName = s"${junctionTableInfo.nameCamelCasedUncapitalized+"s"}For${referencedTableInfo.nameCamelCased}Query"

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

  def formOptionsMethodCode = {
    s"""
def formOptions : Seq[(String, String)] = {
  ${queryObjectName}.list.map{ row =>
    (row.${primaryKeyName}.toString, ${fieldsForSimpleName.map("row." + _).mkString(" + \" \" + ")})
  }
}""".trim()
  }
  
}

package generators.squeryl.models

import generators.slick.utils.TableInfo
import generators.utils.GeneratorHelpers
import scala.slick.model.ForeignKey

trait SquerylDaoGeneratorHelpers extends GeneratorHelpers{

  val rowName : String

  val listName : String

  val tableRowName : String

  val primaryKeyName : String

  val primaryKeyType : String

  val queryObjectName : String

  val fieldsForSimpleName : Seq[String]
  
  def saveMethodCode = {
    
    s"""
def save(${rowName}: ${tableRowName}) : ${primaryKeyType} = {
  inTransaction(${queryObjectName}.insert(${rowName}).id)
}""".trim()
  }

  def saveJunctionMethodCode = {

    s"""
def save(${rowName}: ${tableRowName}) = {
  inTransaction(${queryObjectName}.insert(${rowName}))
}""".trim()
  }
  
  def findAllMethodCode = {
    s"""
def findAll : List[${tableRowName}] = {
  inTransaction(${queryObjectName}.toList)
}""".trim()
  }
  
  def findByIdMethodCode = {
    s"""
def findById(id : ${primaryKeyType}) : Option[${tableRowName}] = {
  inTransaction(${queryObjectName}.lookup(id))
}""".trim()
  }
  
  def deleteMethodCode = {
    s"""
def delete(id : ${primaryKeyType}) = {
  inTransaction(${queryObjectName}.delete(id))
}""".trim()
  }

  def deleteJunctionMethodCode(foreignKeys : Seq[ForeignKey]) = {

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
  }
  
  def updateMethodCode = {
    s"""
def update(updatedRow: ${tableRowName}) = {
  inTransaction(${queryObjectName}.update(updatedRow))
}""".trim()
  }

  def findByForeignKeyMethodCode(referencedTableInfo : TableInfo) = {

    val referencedRow = referencedTableInfo.nameCamelCasedUncapitalized

    val methodName = s"${listName}For${referencedTableInfo.nameCamelCased}"

  s"""
def ${methodName}(${referencedRow} : ${referencedTableInfo.tableRowName}) : List[${tableRowName}] = {
  inTransaction(${referencedRow}.${listName}.toList)
}
""".trim()
  }


  def formOptionsMethodCode = {
    s"""
def formOptions : Seq[(String, String)] = {
  inTransaction{
    ${queryObjectName}.map{ row =>
      (row.id.toString, ${fieldsForSimpleName.map("row." + _).mkString(" + \" \" + ")})
    }.toSeq
  }
}""".trim()
  }
  
}

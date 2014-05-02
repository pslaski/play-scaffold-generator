package generators.slick.models

import generators.utils.StringUtils

trait DaoGeneratorHelpers extends StringUtils{

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
    s"""
def delete(${primaryKeyName}: ${primaryKeyType}) = {
  ${childData.map(child => deleteChilds(child._1, child._2)).mkString("\n")}
  val queryFindById = ${findByCode(primaryKeyName)}

  queryFindById.delete
}""".trim()
  }
  
  def findByCode(fieldName : String) = {
    s"""
for {
    row <- ${queryObjectName} if row.${fieldName} === ${fieldName}
  } yield row""".trim()
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

  def findByForeignKeyMethodCode(foreignKeyName : String, referencedTable : String) = {
  s"""
def ${rowNameCamelCased.uncapitalize+"s"}For${referencedTable}(${foreignKeyName}: Long) : List[${tableRowName}] = {
  ${queryObjectName}.filter(_.${foreignKeyName} === ${foreignKeyName}).list
}""".trim()
}

  def formOptionsMethodCode = {
    s"""
def formOptions : Seq[(String, String)] = {
  ${queryObjectName}.list.map{ row =>
    (row.${primaryKeyName}.toString, ${fieldsForSimpleName.map("row." + _).mkString(" + \" \" + ")})
  }
}""".trim()
  }

  def deleteByForeignKeyMethodCode(foreignKeyName : String, referencedTable : String) = {
    s"""
def delete${rowNameCamelCased+"s"}For${referencedTable}(${foreignKeyName} : Long) = {
  ${rowNameCamelCased.uncapitalize+"s"}For${referencedTable}(${foreignKeyName}) map {  row =>
    delete(row.${primaryKeyName})
  }
}""".trim()
  }

  def deleteChilds(childDao : String, childName : String) = {
    s"${childDao}.delete${childName + "s"}For${queryObjectName}(${primaryKeyName})"
  }
  
}
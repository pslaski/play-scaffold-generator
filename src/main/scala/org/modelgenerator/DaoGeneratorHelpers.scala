package org.modelgenerator

trait DaoGeneratorHelpers {
  
  def saveMethodCode(rowClass : String, primaryKey : String, primaryKeyTpe : String, queryObject : String) = {
    
    val row = rowClass.toLowerCase().dropRight(3)
    
    s"""
def save(${row}: ${rowClass}) : ${primaryKeyTpe} = {
  ${queryObject} returning ${queryObject}.map(_.${primaryKey}) insert(${row})
}""".trim()
  }
  
  def findAllMethodCode(rowClass : String, queryObject : String) = {
    s"""
def findAll : List[${rowClass}] = {
  ${queryObject}.list
}""".trim()
  }
  
  def findByIdMethodCode(rowClass : String, primaryKey : String, primaryKeyTpe : String, queryObject : String) = {
    s"""
def findById(${primaryKey}: ${primaryKeyTpe}) : Option[${rowClass}] = {
  val queryFindById = ${findByCode(queryObject, primaryKey)}

  queryFindById.firstOption
}""".trim()
  }
  
  def deleteMethodCode(primaryKey : String, primaryKeyTpe : String, queryObject : String) = {
    s"""
def delete(${primaryKey}: ${primaryKeyTpe}) = {
  val queryFindById = ${findByCode(queryObject, primaryKey)}

  queryFindById.delete
}""".trim()
  }
  
  def findByCode(queryObject : String, fieldName : String) = {
    s"""
for {
    row <- ${queryObject} if row.${fieldName} === ${fieldName}
  } yield row""".trim()
  }
  
    def updateMethodCode(rowClass : String, primaryKey : String, queryObject : String) = {
    s"""
def update(updatedRow: ${rowClass}) = {
  val queryFindById = for {
    row <- ${queryObject} if row.${primaryKey} === updatedRow.${primaryKey}
  } yield row

  queryFindById.update(updatedRow)
}""".trim()
  }
  
}
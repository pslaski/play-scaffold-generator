package org.modelgenerator

trait GeneratorHelpers {

  def importCode(importPath : String) = "import " + importPath;
  
  def saveMethodCode(rowClass : String, primaryKey : String, queryObject : String) = {
    
    val row = rowClass.toLowerCase().dropRight(3)
    
    s"""
def save(${row}: ${rowClass}) : Long = {
    ${queryObject} returning ${queryObject}.map(_.${primaryKey}) insert(${row})
  }""".trim()
  }
  
  def findAllMethodCode(rowClass : String, queryObject : String) = {
    s"""
def findAll : List[${rowClass}] = {
     ${queryObject}.list
  }""".trim()      
  }
  
  def findByIdMethodCode(rowClass : String, primaryKey : String, queryObject : String) = {
    s"""
  def findById(${primaryKey}: Long) : Option[${rowClass}] = {
    val queryFindById = ${findByCode(queryObject, primaryKey)}

    queryFindById.firstOption
  }""".trim()
  }
  
  def deleteMethodCode(primaryKey : String, queryObject : String) = {
    s"""
  def delete(${primaryKey}: Long) = {
    val queryFindById = ${findByCode(queryObject, primaryKey)}

    queryFindById.delete
  }""".trim()
  }
  
  def findByCode(queryObject : String, fieldName : String) = {
    s"""
    for{
      row <- ${queryObject} if row.${fieldName} === ${fieldName}
    } yield row""".trim()
  }
  
    def updateMethodCode(rowClass : String, primaryKey : String, queryObject : String) = {
    s"""
  def update(updatedRow: ${rowClass}) = {
    val queryFindById = for{
      row <- ${queryObject} if row.${primaryKey} === updatedRow.${primaryKey}
    } yield row

    queryFindById.update(updatedRow)
  }""".trim()
  }
  
  
}
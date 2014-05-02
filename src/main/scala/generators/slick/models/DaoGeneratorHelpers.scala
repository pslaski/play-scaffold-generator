package generators.slick.models

trait DaoGeneratorHelpers {

  val rowName : String

  val tableRowName : String

  val primaryKeyName : String

  val primaryKeyType : String

  val queryObjectName : String
  
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
  
  def deleteMethodCode = {
    s"""
def delete(${primaryKeyName}: ${primaryKeyType}) = {
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
  
}
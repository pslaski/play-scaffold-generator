package generators.models.anorm

import generators.utils.OutputHelpers

object CustomToStatementGenerator extends OutputHelpers {

  def code : String = {
    s"""
object CustomToStatement {

  import java.sql.PreparedStatement
  import anorm.ToStatement

  implicit def sqlDateToStatement: ToStatement[java.sql.Date] = new ToStatement[java.sql.Date] {
    def set(statement: PreparedStatement, index: Int, value: java.sql.Date): Unit =
      statement.setDate(index, value)
  }

  implicit def timeToStatement: ToStatement[java.sql.Time] = new ToStatement[java.sql.Time] {
    def set(statement: PreparedStatement, index: Int, value: java.sql.Time): Unit =
      statement.setTime(index, value)
  }

}
    """.trim()
  }
  
  def indent(code: String): String = code
  
  override def writeToFile(folder:String, pkg: String, fileName: String="CustomToStatement.scala"){
    super.writeToFile(folder, pkg, fileName)
  }
}
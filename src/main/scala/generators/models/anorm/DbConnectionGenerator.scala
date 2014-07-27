package generators.models.anorm

import generators.utils.OutputHelpers

object DbConnectionGenerator extends OutputHelpers {

  def code : String = {
    s"""
import java.sql.Connection

import play.api.db.DB
import play.api.Play.current

//auto-generated object providing implicit db connection
object DbConnection {

  implicit lazy val connection : Connection = DB.getConnection()

}
    """.trim()
  }
  
  def indent(code: String): String = code
  
  override def writeToFile(folder:String, pkg: String, fileName: String="DbConnection.scala"){
    super.writeToFile(folder, pkg, fileName)
  }
}
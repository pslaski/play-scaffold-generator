package generators.models.slick

import generators.utils.OutputHelpers

object DbSessionGenerator extends OutputHelpers {

  def code : String = {
    s"""
import models.Tables
import Tables.profile.simple._
import play.api.db.DB
import play.api.Play.current

//auto-generated object providing implicit db session
object DbSession {

  implicit lazy val session : Session = Database.forDataSource(DB.getDataSource()).createSession()

}
    """.trim()
  }
  
  def indent(code: String): String = code
  
  override def writeToFile(folder:String, pkg: String, fileName: String="DbSession.scala"){
    super.writeToFile(folder, pkg, fileName)
  }
}
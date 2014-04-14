package org.modelgenerator

object DbConnectionGenerator extends OutputHelpers {

  def code : String = {
    s"""
import play.api.db.slick.Config.driver.simple._
import play.api.db.DB
import play.api.Play.current

//auto-generated object providing implicit db session
object DbConnection {

  implicit lazy val session : Session = Database.forDataSource(DB.getDataSource()).createSession()

}
    """.trim()
  }
  
  def indent(code: String): String = code//code.split("\n").mkString("\n"+"  ")
  
  override def writeToFile(folder:String, pkg: String, fileName: String="DbConnection.scala"){
    super.writeToFile(folder, pkg, fileName)
  }
}
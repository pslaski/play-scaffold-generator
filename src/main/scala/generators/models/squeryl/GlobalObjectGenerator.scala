package generators.models.squeryl

import generators.utils._

object GlobalObjectGenerator extends OutputHelpers with GeneratorHelpers{

  private val appConfig = AppConfigParser.getAppConfig

  override def code: String = {
    s"""
${imports}

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    SessionFactory.concreteFactory = app.configuration.getString("db.default.driver") match {
      ${drivers}
      case _ => sys.error("Database adapter for driver not found")
    }
  }

  def getSession(adapter:DatabaseAdapter, app: Application) = Session.create(DB.getConnection()(app), adapter)

}
     """.trim
  }

  override def indent(code: String): String = code

  val h2Driver = "org.h2.Driver"

  val jdbcDriver = appConfig.jdbcDriver

  def imports = (dynamicImports ++ fixedImports).mkString("\n")

  def fixedImports = {
    Seq(importCode("org.squeryl.internals.DatabaseAdapter"),
        importCode("org.squeryl.{Session, SessionFactory}"),
        importCode("play.api.Application"),
        importCode("play.api.db.DB"),
        importCode("play.api.GlobalSettings"))
  }

  def dynamicImports = {
    if(jdbcDriver.equals(h2Driver)) Seq(importCode("org.squeryl.adapters.H2Adapter"))
    else {
      Seq(importCode(s"org.squeryl.adapters.{H2Adapter, ${DriverLoader.jdbcToSquerylDriver(jdbcDriver)}}"))
    }
  }

  def drivers = {
    if(jdbcDriver.equals(h2Driver)) Seq(driverCase(h2Driver, DriverLoader.jdbcToSquerylDriver(h2Driver)))
    else {
      Seq(driverCase(h2Driver, DriverLoader.jdbcToSquerylDriver(h2Driver)),
          driverCase(jdbcDriver, DriverLoader.jdbcToSquerylDriver(jdbcDriver)))
    }
  }.mkString("\n\t\t\t")

  def driverCase(driver : String, adapter : String) = {
    s"""case Some("${driver}") => Some(() => getSession(new ${adapter}, app))"""
  }

  override def writeToFile(folder:String, pkg: String, fileName: String="Global.scala"){
    writeStringToFile(code, folder, pkg, fileName)
  }
}

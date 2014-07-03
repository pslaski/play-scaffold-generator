package generators.utils

import java.io.File

import com.typesafe.config.Config

import scala.slick.driver.JdbcProfile.capabilities

object AppConfigParser extends ConfigUtils{

  private var appConfig : AppConfig = null

  def parse(configFile : File, appName : String) = {
    val config = parseFile(configFile)

    appConfig = new AppConfig(config, appName)
  }

  def getAppConfig = appConfig

}

class AppConfig(config : Config, appName : String) extends ConfigUtils{

  val applicationName = appName
  
	val jdbcDriver = getStringOrDefault("db.default.driver", config, "org.h2.Driver")
	
	val url = getStringOrDefault("db.default.url", config, "jdbc:h2:mem:play")
	
	val user = getStringOrDefault("db.default.user", config)
	
	val password = getStringOrDefault("db.default.password", config)
	
	val modelsPackage = getStringOrDefault("generator.default.modelsDir", config, "models")

  val controllersPackage = getStringOrDefault("generator.default.controllersDir", config, "controllers")

  val viewsPackage = getStringOrDefault("generator.default.viewsDir", config, "views")
	  
	val utilsPackage = getStringOrDefault("generator.default.utilsDir", config, "utils")

  lazy val slickDriver = DriverLoader.slickDriver(jdbcDriver)
  lazy val slickDriverPath = DriverLoader.slickDriverPath(jdbcDriver)

  lazy val isInsertReturnOtherEnabled = slickDriver.capabilities.contains(capabilities.returnInsertOther)

}
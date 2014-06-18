package generators.utils

import java.io.File

class Config(configFile : File, appName : String) extends ConfigUtils{
	
	private val config = parseFile(configFile)

  val applicationName = appName
  
	val jdbcDriver = getStringOrDefault("db.default.driver", config, "org.h2.Driver")
	
	val url = getStringOrDefault("db.default.url", config, "jdbc:h2:mem:play")
	
	val user = getStringOrDefault("db.default.user", config)
	
	val password = getStringOrDefault("db.default.password", config)
	
	val modelsPackage = getStringOrDefault("generator.default.modelsDir", config, "models")

  val controllersPackage = getStringOrDefault("generator.default.controllersDir", config, "controllers")

  val viewsPackage = getStringOrDefault("generator.default.viewsDir", config, "views")
	  
	val utilsPackage = getStringOrDefault("generator.default.utilsDir", config, "utils")

}
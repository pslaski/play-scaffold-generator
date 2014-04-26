package org.modelgenerator

import sbt._
import java.io.File
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigException

class Config(configFile : File) {
	
	private val config = ConfigFactory.parseFile(configFile)
  
	val jdbcDriver = getStringOrDefault("db.default.driver","org.h2.Driver")
	
	val url = getStringOrDefault("db.default.url","jdbc:h2:mem:play")
	
	val user = getStringOrDefault("db.default.user","")
	
	val password = getStringOrDefault("db.default.password","")
	
	val modelsPackage = getStringOrDefault("generator.default.modelsDir","models")

  val controllersPackage = getStringOrDefault("generator.default.controllersDir","controllers")
	  
	val utilsPackage = getStringOrDefault("generator.default.utilsDir","utils")

	
	def getStringOrDefault(key : String, default : String) : String = {
	  try{
	    config.getString(key)
	  }
	  catch {
	    case missing : ConfigException.Missing => default
	  }
	}
}
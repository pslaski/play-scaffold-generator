package org.modelgenerator

import sbt._
import Keys._
import scala.slick.model.codegen.SourceCodeGenerator
import com.typesafe.config.ConfigFactory



object ModelGeneratorPlugin extends Plugin {

  override lazy val settings = Seq(slick <<= slickCodeGenTask)

  
		  // code generation task
  lazy val slick = TaskKey[Seq[File]]("gen-tables")
  lazy val slickCodeGenTask = baseDirectory in Compile map { baseDir =>
    
    val configFile = (baseDir / "/conf/application.conf").getAbsoluteFile

	val config = ConfigFactory.parseFile(configFile)
	
	val jdbcDriver = config.getString("db.default.driver")
	
	val url = config.getString("db.default.url")
	
	val user = config.getString("db.default.user")
	
	val password = config.getString("db.default.password")
    
    val outputDir = (baseDir / "app").getPath
    
    val modelsPackage = "models"
      
    val utilsPackage = "utils"
      
    TablesGenerator.generate(Array(jdbcDriver, url, outputDir, modelsPackage, user, password))
    
    DbConnectionGenerator.writeToFile(outputDir, utilsPackage)

    DaoObjectGenerator.generate(Array(jdbcDriver, url, outputDir, modelsPackage, user, password))
    
    val modelFileName = outputDir + "/" + modelsPackage + "/Tables.scala"
    val dbConnectionFileName = outputDir + "/" + utilsPackage + "/DbConnection.scala"
    Seq(file(modelFileName), file(dbConnectionFileName))
  }
  
}
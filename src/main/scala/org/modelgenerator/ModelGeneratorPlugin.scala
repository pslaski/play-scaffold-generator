package org.modelgenerator

import sbt._
import Keys._

object ModelGeneratorPlugin extends Plugin {

  override lazy val settings = Seq(slick <<= slickCodeGenTask)

  
		  // code generation task
  lazy val slick = TaskKey[Seq[File]]("gen-tables")
  lazy val slickCodeGenTask = (baseDirectory in Compile, name) map { (baseDir, appName) =>
    
    val configFile = (baseDir / "/conf/application.conf").getAbsoluteFile
    
    val config = new Config(configFile, appName)
	
    val outputDir = (baseDir / "app").getPath
      
    TablesGenerator.generate(config, outputDir)
    
    DbConnectionGenerator.writeToFile(outputDir, config.utilsPackage)

    DaoObjectGenerator.generate(config, outputDir)

    ControllerGenerator.generate(config,outputDir)

    ViewGenerator.generate(config, outputDir)
    
    val modelFileName = outputDir + "/" + config.modelsPackage + "/Tables.scala"
    val dbConnectionFileName = outputDir + "/" + config.utilsPackage + "/DbConnection.scala"
    Seq(file(modelFileName), file(dbConnectionFileName))
  }
  
}
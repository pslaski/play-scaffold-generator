import generators.slick.views.ViewGenerator
import generators.utils.Config
import sbt._
import Keys._
import generators.slick.models.{DbConnectionGenerator, DaoObjectGenerator, TablesGenerator}
import generators.slick.controllers.ControllerGenerator

object ScaffoldPlugin extends Plugin {

  override lazy val settings = Seq(slick <<= slickCodeGenTask)

  
		  // code generation task
  lazy val slick = TaskKey[Seq[File]]("scaffold-slick")
  lazy val slickCodeGenTask = (baseDirectory in Compile, name, streams) map { (baseDir, appName, stream) =>
    
    val configFile = (baseDir / "/conf/application.conf").getAbsoluteFile
    
    val config = new Config(configFile, appName)
	
    val outputDir = (baseDir / "app").getPath

    stream.log.info("Start scaffold....")
      
    TablesGenerator.generate(config, outputDir)

    stream.log.info("Generating tables completed....")
    
    DbConnectionGenerator.writeToFile(outputDir, config.utilsPackage)

    stream.log.info("Generating DBconnection helper completed....")

    DaoObjectGenerator.generate(config, outputDir)

    stream.log.info("Generating Dao objects completed....")

    ControllerGenerator.generate(config,outputDir)

    stream.log.info("Generating controllers and routes completed....")

    ViewGenerator.generate(config, outputDir)

    stream.log.info("Generating views and css completed....")
    
    val modelFileName = outputDir + "/" + config.modelsPackage + "/Tables.scala"
    val dbConnectionFileName = outputDir + "/" + config.utilsPackage + "/DbConnection.scala"
    Seq(file(modelFileName), file(dbConnectionFileName))
  }
  
}
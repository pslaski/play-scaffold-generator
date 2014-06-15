import generators.slick.views.ViewGenerator
import generators.squeryl.models.{SquerylDaoObjectGenerator, SchemaGenerator}
import generators.utils.Config
import sbt._
import Keys._
import generators.slick.models.{DbConnectionGenerator, SlickDaoObjectGenerator, TablesGenerator}
import generators.slick.controllers.{CustomFormattersGenerator, ControllerGenerator}

object ScaffoldPlugin extends Plugin {

  override lazy val settings = Seq(slick <<= slickCodeGenTask,
                                   squeryl <<= squerylCodeGenTask)

  
	// slick code generation task
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

    SlickDaoObjectGenerator.generate(config, outputDir)

    stream.log.info("Generating Dao objects completed....")

    CustomFormattersGenerator.writeToFile(outputDir, config.utilsPackage)

    stream.log.info("Generating custom formatters completed....")

    ControllerGenerator.generate(config,outputDir)

    stream.log.info("Generating controllers and routes completed....")

    ViewGenerator.generate(config, outputDir)

    stream.log.info("Generating views and css completed....")
    
    val modelFileName = outputDir + "/" + config.modelsPackage + "/Tables.scala"
    Seq(file(modelFileName))
  }

  // squeryl code generation task
  lazy val squeryl = TaskKey[Seq[File]]("scaffold-squeryl")
  lazy val squerylCodeGenTask = (baseDirectory in Compile, name, streams) map { (baseDir, appName, stream) =>

    val configFile = (baseDir / "/conf/application.conf").getAbsoluteFile

    val config = new Config(configFile, appName)

    val outputDir = (baseDir / "app").getPath

    stream.log.info("Start scaffold....")

    SchemaGenerator.generate(config, outputDir)

    stream.log.info("Generating schema completed....")

    SquerylDaoObjectGenerator.generate(config, outputDir)

    stream.log.info("Generating Dao objects completed....")

    CustomFormattersGenerator.writeToFile(outputDir, config.utilsPackage)

    stream.log.info("Generating custom formatters completed....")

    ControllerGenerator.generate(config,outputDir)

    stream.log.info("Generating controllers and routes completed....")

    ViewGenerator.generate(config, outputDir)

    stream.log.info("Generating views and css completed....")

    val modelFileName = outputDir + "/" + config.modelsPackage + "/Tables.scala"
    Seq(file(modelFileName))
  }
  
}
import generators.models.anorm.{DbConnectionGenerator, ClassesAndParsersGenerator}
import generators.models.slick.{DbSessionGenerator, TablesGenerator, SlickDaoObjectGenerator}
import generators.models.squeryl.{GlobalObjectGenerator, SquerylDaoObjectGenerator, SchemaGenerator}
import generators.views.ViewGenerator
import generators.utils.{AppConfigParser, TablesConfigParser}
import sbt._
import Keys._
import generators.controllers.{CustomFormattersGenerator, ControllerGenerator}

object ScaffoldPlugin extends Plugin {

  override lazy val settings = Seq(slick <<= slickCodeGenTask,
                                   squeryl <<= squerylCodeGenTask,
                                    anorm <<= anormCodeGenTask)

	// slick code generation task
  lazy val slick = TaskKey[Unit]("scaffold-slick")
  lazy val slickCodeGenTask = (baseDirectory in Compile, name, streams) map { (baseDir, appName, stream) =>

    val configFile = (baseDir / "/conf/application.conf").getAbsoluteFile
    
    AppConfigParser.parse(configFile, appName)

    val appConfig = AppConfigParser.getAppConfig

    val scaffoldConfig = (baseDir / "/conf/scaffold-config.conf").getAbsoluteFile

    TablesConfigParser.parse(scaffoldConfig)
	
    val outputDir = (baseDir / "app").getPath

    stream.log.info("Start scaffold....")
      
    TablesGenerator.generate(outputDir)

    stream.log.info("Generating tables completed....")
    
    DbSessionGenerator.writeToFile(outputDir, appConfig.utilsPackage)

    stream.log.info("Generating DB session helper completed....")

    SlickDaoObjectGenerator.generate(outputDir)

    stream.log.info("Generating Dao objects completed....")

    CustomFormattersGenerator.writeToFile(outputDir, appConfig.utilsPackage)

    stream.log.info("Generating custom formatters completed....")

    ControllerGenerator.generate(outputDir)

    stream.log.info("Generating controllers and routes completed....")

    ViewGenerator.generate(outputDir)

    stream.log.info("Generating views and css completed....")

  }

  // squeryl code generation task
  lazy val squeryl = TaskKey[Unit]("scaffold-squeryl")
  lazy val squerylCodeGenTask = (baseDirectory in Compile, name, streams) map { (baseDir, appName, stream) =>

    val configFile = (baseDir / "/conf/application.conf").getAbsoluteFile

    AppConfigParser.parse(configFile, appName)

    val appConfig = AppConfigParser.getAppConfig

    val scaffoldConfig = (baseDir / "/conf/scaffold-config.conf").getAbsoluteFile

    TablesConfigParser.parse(scaffoldConfig)

    val outputDir = (baseDir / "app").getPath

    stream.log.info("Start scaffold....")

    GlobalObjectGenerator.writeToFile(outputDir, "")

    stream.log.info("Generating Global Object completed....")

    SchemaGenerator.generate(outputDir)

    stream.log.info("Generating schema completed....")

    SquerylDaoObjectGenerator.generate(outputDir)

    stream.log.info("Generating Dao objects completed....")

    CustomFormattersGenerator.writeToFile(outputDir, appConfig.utilsPackage)

    stream.log.info("Generating custom formatters completed....")

    ControllerGenerator.generate(outputDir)

    stream.log.info("Generating controllers and routes completed....")

    ViewGenerator.generate(outputDir)

    stream.log.info("Generating views and css completed....")

  }

    // anorm code generation task
  lazy val anorm = TaskKey[Unit]("scaffold-anorm")
  lazy val anormCodeGenTask = (baseDirectory in Compile, name, streams) map { (baseDir, appName, stream) =>

    val configFile = (baseDir / "/conf/application.conf").getAbsoluteFile

    AppConfigParser.parse(configFile, appName)

    val appConfig = AppConfigParser.getAppConfig

    val scaffoldConfig = (baseDir / "/conf/scaffold-config.conf").getAbsoluteFile

    TablesConfigParser.parse(scaffoldConfig)

    val outputDir = (baseDir / "app").getPath

    stream.log.info("Start scaffold....")

    //GlobalObjectGenerator.writeToFile(outputDir, "")

    //stream.log.info("Generating Global Object completed....")

    ClassesAndParsersGenerator.generate(outputDir)

    stream.log.info("Generating classes and parsers completed....")

    DbConnectionGenerator.writeToFile(outputDir, appConfig.utilsPackage)

    stream.log.info("Generating DB connection helper completed....")

    //SquerylDaoObjectGenerator.generate(outputDir)

    //stream.log.info("Generating Dao objects completed....")

    CustomFormattersGenerator.writeToFile(outputDir, appConfig.utilsPackage)

    stream.log.info("Generating custom formatters completed....")

    ControllerGenerator.generate(outputDir)

    stream.log.info("Generating controllers and routes completed....")

    ViewGenerator.generate(outputDir)

    stream.log.info("Generating views and css completed....")

  }
  
}
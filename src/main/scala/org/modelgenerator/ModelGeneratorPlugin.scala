package org.modelgenerator

import sbt._
import Keys._
import scala.slick.model.codegen.SourceCodeGenerator
import com.typesafe.config.ConfigFactory



object ModelGeneratorPlugin extends Plugin {

  override lazy val settings = Seq(commands += myCommand)

  lazy val myCommand =
    Command.command("gen-tables") { (state: State) =>

      val configFile = (state.configuration.baseDirectory() / "/conf/application.conf").getAbsoluteFile

      val config = ConfigFactory.parseFile(configFile)

      val jdbcDriver = config.getString("db.default.driver")

      val url = config.getString("db.default.url")

      val user = config.getString("db.default.user")

      val password = config.getString("db.default.password")

/*     val sourceGenerator = SourceCodeGenerator.main(
        Array("scala.slick.driver.PostgresDriver",
          jdbcDriver,
          url,
          (state.configuration.baseDirectory() / "app").getPath,
          "models",
          user,
          password)
      )*/

      state
    }
  
}
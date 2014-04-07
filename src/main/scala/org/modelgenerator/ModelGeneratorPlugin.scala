package org.modelgenerator

import sbt._
import Keys._
import scala.slick.model.codegen.SourceCodeGenerator

object ModelGeneratorPlugin extends Plugin {

  override lazy val settings = Seq(commands += myCommand)

  lazy val myCommand =
    Command.command("gen-tables") { (state: State) =>

      println(state.configuration.baseDirectory())
      val sourceGenerator = SourceCodeGenerator.main(
        Array("scala.slick.driver.PostgresDriver",
          "org.postgresql.Driver",
          "jdbc:postgresql://localhost:5432/shakespeare",
          (state.configuration.baseDirectory() / "app").getPath,
          "models",
          "play_user",
          "12345")
      )

      val extracted: Extracted = Project.extract(state)
      println(extracted.currentRef.project)
      state
    }
  
}
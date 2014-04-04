package org.modelgenerator

import sbt._
import Keys._

object ModelGeneratorPlugin extends Plugin {

  override lazy val settings = Seq(commands += myCommand)

  lazy val myCommand =
    Command.command("printAllSettings") { (state: State) =>
      val extracted: Extracted = Project.extract(state)
      extracted.currentProject.settings map println
      state
    }
  
}
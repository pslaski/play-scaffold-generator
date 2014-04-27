package org.modelgenerator

import scala.slick.model.{Table, Column}
import java.io.{BufferedWriter, FileWriter, File}

object ViewGenerator {
  def generate(config : Config, outputFolder : String) = {

    val jdbcDriver = config.jdbcDriver
    val url = config.url
    val pkg = config.viewsPackage
    val user = config.user
    val password = config.password

    val slickDriver = DriverLoader.slickDriver(jdbcDriver)

    val db = slickDriver.simple.Database.forURL(url,driver=jdbcDriver, user = user, password = password)
    val model = db.withSession(slickDriver.createModel(_))

    new MainLayoutViewGenerator(model, config.applicationName).writeToFile(outputFolder, pkg)

    MainCssGenerator.writeToFile("public", "stylesheets")

    model.tables map { table =>
      val outputPkg = pkg + "." + table.name.table
      new ViewGenerator(table, outputFolder, outputPkg).generate
    }
  }
}

class ViewGenerator(table : Table, folder:String, pkg: String)  {

  val createFormViewGenerator = new CreateFormViewGenerator(table)

  val editFormViewGenerator = new EditFormViewGenerator(table)

  val listViewGenerator = new ListViewGenerator(table)

  val showViewGenerator = new ShowViewGenerator(table)

  def generate = {
    createFormViewGenerator.writeToFile(folder, pkg, "createForm.scala.html")
    editFormViewGenerator.writeToFile(folder, pkg, "editForm.scala.html")
    listViewGenerator.writeToFile(folder, pkg, "list.scala.html")
    showViewGenerator.writeToFile(folder, pkg, "show.scala.html")
  }

}

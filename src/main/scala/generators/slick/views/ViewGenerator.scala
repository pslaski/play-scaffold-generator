package generators.slick.views

import scala.slick.model.Table
import generators.slick.css.MainCssGenerator
import generators.slick.utils.{ForeignKeyInfo, DriverLoader}
import generators.utils.{ModelProvider, Config}

object ViewGenerator {
  def generate(config : Config, outputFolder : String) = {

    val pkg = config.viewsPackage

    val model = new ModelProvider(config).model

    val foreignKeyInfo = new ForeignKeyInfo(model)

    new MainLayoutViewGenerator(model, config.applicationName).writeToFile(outputFolder, pkg)

    MainCssGenerator.writeToFile("public", "stylesheets")

    model.tables map { table =>
      val outputPkg = pkg + "." + table.name.table
      new ViewGenerator(table, foreignKeyInfo, outputFolder, outputPkg).generate
    }
  }
}

class ViewGenerator(table : Table, foreignKeyInfo : ForeignKeyInfo, folder:String, pkg: String)  {

  val createFormViewGenerator = new CreateFormViewGenerator(table)

  val editFormViewGenerator = new EditFormViewGenerator(table)

  val listViewGenerator = new ListViewGenerator(table)

  val showViewGenerator = new ShowViewGenerator(table, foreignKeyInfo)

  def generate = {
    createFormViewGenerator.writeToFile(folder, pkg, "createForm.scala.html")
    editFormViewGenerator.writeToFile(folder, pkg, "editForm.scala.html")
    listViewGenerator.writeToFile(folder, pkg, "list.scala.html")
    showViewGenerator.writeToFile(folder, pkg, "show.scala.html")
  }

}

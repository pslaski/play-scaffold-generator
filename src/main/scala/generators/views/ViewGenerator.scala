package generators.views

import scala.slick.model.Table
import generators.css.MainCssGenerator
import generators.utils._

object ViewGenerator {
  def generate(outputFolder : String) = {

    val appConfig = AppConfigParser.getAppConfig

    val pkg = appConfig.viewsPackage

    val model = new ModelProvider(appConfig).model

    val foreignKeyInfo = new ForeignKeyInfo(model)

    new MainLayoutViewGenerator(model).writeToFile(outputFolder, pkg)
    IndexViewGenerator.writeToFile(outputFolder, pkg)

    MainCssGenerator.writeToFile("public", "stylesheets")

    model.tables map { table =>
      val outputPkg = pkg + "." + table.name.table.toLowerCase
      val tableInfo = new TableInfo(table)

      if(tableInfo.isSimpleJunctionTable) new JunctionViewGenerator(table, outputFolder, outputPkg).generate
      else new FullViewGenerator(table, foreignKeyInfo, outputFolder, outputPkg).generate
    }
  }
}

class FullViewGenerator(table : Table, foreignKeyInfo : ForeignKeyInfo, folder:String, pkg: String)  {

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

class JunctionViewGenerator(table : Table, folder:String, pkg: String)  {

  val createFormViewGenerator = new CreateFormViewJunctionGenerator(table)

  def generate = {
    createFormViewGenerator.writeToFile(folder, pkg, "createForm.scala.html")
  }

}

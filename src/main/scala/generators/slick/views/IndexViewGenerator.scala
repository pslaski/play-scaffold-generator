package generators.slick.views

import generators.utils.OutputHelpers

class IndexViewGenerator(appName : String) extends OutputHelpers{
  override def code: String = {
s"""
@main("${appName}") {

    <h1>Welcome to ${appName}</h1>

}
 """.trim
  }

  override def indent(code: String): String = code

  override def writeToFile(folder:String, pkg: String, fileName: String="index.scala.html") {
    writeStringToFile(code, folder, pkg, fileName)
  }
}

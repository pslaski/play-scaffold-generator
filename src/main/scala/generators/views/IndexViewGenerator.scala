package generators.views

import generators.utils.{AppConfigParser, OutputHelpers}

object IndexViewGenerator extends OutputHelpers{

  val appName = AppConfigParser.getAppConfig.applicationName

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

package generators.slick.views

import generators.utils.{StringUtils, OutputHelpers}

trait ViewHelpers extends OutputHelpers with StringUtils{

  val title : String

  def importCodeView(importPath : String) = "@import " + importPath

  def imports : String

  val arguments : Seq[(String, String)]

  def viewArguments = {
    "@(" + (arguments map printArg).mkString(", ") + ")"
  }

  private def printArg(arg : (String, String)) = arg._1 + " : " + arg._2

  def bodyCode : String

  def mainCode = {
    s"""
@main("${title}"){

     ${bodyCode}

}""".trim()
  }

  def code = {
    s"""
${viewArguments}

${imports}

${mainCode}
""".trim()
  }

  override def indent(code: String) = code

  override def writeToFile(folder:String, pkg: String, fileName: String) {
    super.writeStringToFile(code, folder, pkg, fileName)
  }
}

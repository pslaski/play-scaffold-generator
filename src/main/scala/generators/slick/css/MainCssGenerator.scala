package generators.slick.css

import generators.utils.OutputHelpers

object MainCssGenerator extends OutputHelpers{
  override def code: String = {
    s"""
body {
  padding-top: 50px;
}

div.content-wrapper {
    padding: 20px 10px 10px 10px;
}

.panel-heading > .btn-group {
    top: -18px;
}
     """.trim
  }

  override def indent(code: String): String = code

  override def writeToFile(folder:String, pkg: String, fileName: String="main.css") {
    writeStringToFile(code, folder, pkg, fileName)
  }
}

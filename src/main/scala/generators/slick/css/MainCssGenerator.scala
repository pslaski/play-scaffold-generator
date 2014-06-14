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

.form-group > .error span.help-inline {
    background-color: #f2dede;
    color: #a94442;
    padding: 5px 10px 5px 10px;
    border: 1px solid #ebccd1;
    border-radius: 4px;
    margin: 7px 0 7px 0;
    display: inline-block;
}

.table-middle td {
    vertical-align: middle !important;
}

.show-details {
    margin-top: 7px;
}

.show-group {
    min-height: 22px;
    background-color: #f5f5f5;
}
     """.trim
  }

  override def indent(code: String): String = code

  override def writeToFile(folder:String, pkg: String, fileName: String="main.css") {
    writeStringToFile(code, folder, pkg, fileName)
  }
}

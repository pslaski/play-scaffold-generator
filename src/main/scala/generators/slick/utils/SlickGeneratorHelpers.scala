package generators.slick.utils

import generators.utils.StringUtils

trait SlickGeneratorHelpers extends StringUtils {

  def importCode(importPath : String) = "import " + importPath;

  def standardColumnName(name : String) = name.toLowerCase.toCamelCase.uncapitalize

}
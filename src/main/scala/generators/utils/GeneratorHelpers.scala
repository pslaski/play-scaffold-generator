package generators.utils

trait GeneratorHelpers extends StringUtils {

  def importCode(importPath : String) = "import " + importPath;

  def standardColumnName(name : String) = name.toLowerCase.toCamelCase.uncapitalize

}
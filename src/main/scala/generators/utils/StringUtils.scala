package generators.utils

trait StringUtils {

  implicit class StringOperations(val str: String){

    final def toCamelCase: String = str.toLowerCase
      .split("_")
      .map(_.capitalize)
      .mkString("")

    final def uncapitalize: String = str(0).toString.toLowerCase + str.tail
  }
}

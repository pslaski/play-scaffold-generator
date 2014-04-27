package org.modelgenerator

trait StringUtils {

  implicit class StringOperations(val str: String){

    final def toCamelCase: String = str.toLowerCase
      .split("_")
      .map(_.capitalize)
      .mkString("")
  }
}

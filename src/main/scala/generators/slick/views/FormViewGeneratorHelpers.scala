package generators.slick.views

import scala.slick.model.Column

trait FormViewGeneratorHelpers{

  val columns : Seq[Column]

  val controllerName : String

  val formName : String

  val submitButtonText : String

  val formAction : String

  val primaryKeyName : String

  val primaryKeyDefaultValue : String = "@value"

  val foreignKeys : Seq[(String, String)]

  def form = {
    s"""
  @form(routes.${controllerName}.${formAction}) {

  <fieldset>

  ${formFields}

  </fieldset>

${actions}

}""".trim()
  }

  def formFields : String = {
    (columns map formField).mkString("\n")
  }

  def formField(column : Column) = {
    if(column.name.toLowerCase.equals(primaryKeyName)){
      inputPrimaryKeyCode(column.name.toLowerCase)
    }
    else if(foreignKeys.exists(_._1.equals(column.name))){
      if(column.nullable) selectOptionalCode(column.name.toLowerCase, foreignKeys.find(_._1.equals(column.name)).get._2)
      else selectCode(column.name.toLowerCase, foreignKeys.find(_._1.equals(column.name)).get._2)
    }
    else {
      convertTypeToInput(column)
    }
  }

  def convertTypeToInput(column : Column) = {
    val (name, tpe) = (column.name.toLowerCase, column.tpe)
    tpe match {
      case "java.sql.Date" => inputDateCode(name)
      case "Boolean" => checkboxCode(name)
      case "java.sql.Blob" => inputFileCode(name)
      case "java.sql.Time" => inputDateCode(name)
      case "java.sql.Timestamp" => inputDateCode(name)
      case "java.sql.Clob" => inputFileCode(name)
      case _ => inputTextCode(name)
    }
  }

  def inputTextCode(fieldName : String) = {
    s"""@inputText(${formName}("${fieldName}"), '_label -> "${fieldName}")"""
  }

  def inputPrimaryKeyCode(fieldName : String) = {
    s"""
@input(${formName}("${fieldName}"), '_label -> None, '_showConstraints -> false) { (id, name, value, args) =>
    <input type="hidden" name="@name" id="@id" value="${primaryKeyDefaultValue}" @toHtmlArgs(args)>
}""".trim()
    }

  def inputDateCode(fieldName : String) = {
    s"""@inputDate(${formName}("${fieldName}"), '_label -> "${fieldName}")"""
  }

  def inputFileCode(fieldName : String) = {
    s"""@inputFile(${formName}("${fieldName}"), '_label -> "${fieldName}")"""
  }

  def checkboxCode(fieldName : String) = {
    s"""@checkbox(${formName}("${fieldName}"), '_label -> "${fieldName}")"""
  }

  def selectCode(fieldName : String, optionsName : String) = {
    s"""@select(${formName}("${fieldName}"), ${optionsName}Options, '_label -> "${fieldName}")"""
  }

  def selectOptionalCode(fieldName : String, optionsName : String) = {
    s"""@select(${formName}("${fieldName}"), ${optionsName}Options, '_label -> "${fieldName}", '_default -> "--${fieldName}--")"""
  }

  def actions = {
    s"""
<div class="actions">
    <input type="submit" value="${submitButtonText}" class="btn primary"> or
    <a href="@routes.${controllerName}.list" class="btn">Cancel</a>
</div>""".trim()
  }
}

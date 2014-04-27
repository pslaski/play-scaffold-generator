package org.modelgenerator

import scala.slick.model.{Column, Table}

class CreateFormViewGenerator(table : Table) extends GeneratorHelpers with ViewHelpers with FormViewGeneratorHelpers{

  override val columns: Seq[Column] = table.columns

  val tableName = table.name.table

  override val title: String = "Add new " + tableName

  val tableRowName = tableName.toCamelCase + "Row"

  val formName = tableName + "Form"

  val controllerName = tableName.toCamelCase + "Controller"

  override val submitButtonText: String = "Save"

  override val formAction: String = "save"

  override val arguments = Seq((formName, "Form[Tables." + tableRowName + "]"))

  override val primaryKeyDefaultValue = "0"

  override def imports: String = {
    Seq(importCodeView("helper._"),
        importCodeView("helper.twitterBootstrap._")).mkString("\n")
  }

  override def bodyCode: String = {
    s"""
<h1>Add new ${tableName}</h1>
  ${form}
""".trim()
  }
}

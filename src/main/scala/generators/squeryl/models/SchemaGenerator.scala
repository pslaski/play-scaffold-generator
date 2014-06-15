package generators.squeryl.models

import generators.slick.utils.{ForeignKeyInfo, TableInfo}
import generators.utils.{Config, GeneratorHelpers, ModelProvider, OutputHelpers}

import scala.slick.model.{Column, ForeignKey, Model}

object SchemaGenerator {
  def generate(config : Config, outputFolder : String) = {

    val pkg = config.modelsPackage

    val model = new ModelProvider(config).model

    new SchemaGenerator(model).writeToFile(outputFolder, pkg)
  }
}

class SchemaGenerator(model : Model) extends OutputHelpers with GeneratorHelpers {

  val foreignKeyInfo = new ForeignKeyInfo(model)

  val objectName = "Tables"

  val allTablesInfo = model.tables.map(new TableInfo(_))

  override def code: String = objectCode

  override def indent(code: String): String = code

  def objectCode : String = {
    s"""
${imports}

object ${objectName} extends Schema {

  ${tables}

  ${relations}

  ${classes}
}
""".trim()
  }

  def imports : String = {
    Seq(importCode("org.squeryl.{Schema, KeyedEntity}"),
        importCode("org.squeryl.PrimitiveTypeMode._"),
        importCode("org.squeryl.dsl.CompositeKey2"),
        importCode("org.squeryl.annotations.Column"))
        .mkString("\n")
  }

  def tables : String = {
    allTablesInfo.map{ tableInfo =>
      printTable(tableInfo)
    }.mkString("\n\n\t")
  }

  def printTable(tableInfo : TableInfo) = {
    s"""val ${tableInfo.queryObjectName} = table[${tableInfo.tableRowName}]("${tableInfo.name}")"""
  }

  def relations : String = {
    allTablesInfo.filter(_.foreignKeys.nonEmpty).map{ tableInfo =>
      if(tableInfo.isJunctionTable) printManyToManyRelation(tableInfo)
      else printOneToManyRelations(tableInfo)
    }.mkString("\n\n\t")
  }

  def printManyToManyRelation(tableInfo : TableInfo) = {

    val (leftFk, rightFk) = (tableInfo.foreignKeys(0), tableInfo.foreignKeys(1))

    val leftTableInfo = new TableInfo(model.tablesByName(leftFk.referencedTable))

    val rightTableInfo = new TableInfo(model.tablesByName(rightFk.referencedTable))

    val leftRefColumn = standardColumnName(leftFk.referencingColumns.head.name)

    val rightRefColumn = standardColumnName(rightFk.referencingColumns.head.name)

    val relationName = leftTableInfo.nameCamelCasedUncapitalized + "To" + rightTableInfo.nameCamelCased

    s"""
  val ${relationName} = {
    manyToManyRelation(${leftTableInfo.queryObjectName}, ${rightTableInfo.queryObjectName}, "${tableInfo.name}").via[${tableInfo.tableRowName}]((a, b, junction) => (junction.${leftRefColumn} === a.id, junction.${rightRefColumn} === b.id))
  }
     """.trim()
  }

  def printOneToManyRelations(tableInfo : TableInfo) = {

    tableInfo.foreignKeys.map(fk => printOneToManyRelation(tableInfo, fk)).mkString("\n\n")

  }

  def printOneToManyRelation(referencingTableInfo : TableInfo, foreignKey : ForeignKey) = {

    val referencedTableInfo = new TableInfo(model.tablesByName(foreignKey.referencedTable))

    val relationName = referencedTableInfo.nameCamelCasedUncapitalized + "To" + referencingTableInfo.nameCamelCased

    val referencingColumn = standardColumnName(foreignKey.referencingColumns.head.name)

    val referencedValue = {
      if (foreignKey.referencingColumns.head.nullable) "Option(a.id)"
      else "a.id"
    }

        s"""
  val ${relationName} = {
    oneToManyRelation(${referencedTableInfo.queryObjectName}, ${referencingTableInfo.queryObjectName}).via((a, b) => ${referencedValue} === b.${referencingColumn})
  }
     """.trim()
  }

  def classes = {
    allTablesInfo.map{ tableInfo =>
      if(tableInfo.isJunctionTable) printJunctionClass(tableInfo)
      else printStandardClass(tableInfo)
    }.mkString("\n\n\t")
  }

  def printJunctionClass(tableInfo : TableInfo) = {

    val fkTypes = tableInfo.foreignKeys.map(_.referencingColumns.head.tpe).mkString(", ")

    val fkColumns = tableInfo.foreignKeys.map( fk => standardColumnName(fk.referencingColumns.head.name)).mkString(", ")

    s"""
  case class ${tableInfo.tableRowName}(${printJunctionClassColumns(tableInfo)}) extends KeyedEntity[CompositeKey2[${fkTypes}]] {
    override def id = compositeKey(${fkColumns})

    ${printConstructor(tableInfo)}
  }
 """.trim()
  }

  def printJunctionClassColumns(tableInfo : TableInfo) : String = {
    tableInfo.columns.map(printColumn(_)).mkString(", \n\t\t\t")
  }

  def printConstructor(tableInfo : TableInfo) : String = {

    val initialiseValues = tableInfo.columns.map(convertTypeToInitValue(_)).mkString(", ")

    s"""def this() = this(${initialiseValues})"""
  }

  def convertTypeToInitValue(column : Column) = {
    if(column.nullable) s"Some(${convertTypeToDefaultConstructor(column.tpe)})"
    else convertTypeToDefaultConstructor(column.tpe)
  }

  def convertTypeToDefaultConstructor(tpe : String) = {
    tpe match {
      case "String" => "\"\""
      case "Int" => "0"
      case "Long" => "0l"
      case "scala.math.BigDecimal" => "scala.math.BigDecimal(0)"
      case "java.sql.Date" => "new java.sql.Date(0)"
      case "Boolean" => "new Boolean()"
      case "Byte" => "0" // not supported
      case "Short" => "0" // not supported
      case "Float" => "0"
      case "Double" => "0"
      case "java.sql.Blob" => "\"\"" // not supported
      case "java.sql.Time" => "new java.sql.Time(0)" // not supported
      case "java.sql.Timestamp" => "new java.sql.Timestamp(0)"
      case "java.sql.Clob" => "\"\""// not supported
      case _ => "\"\""
    }
  }

  def printStandardClass(tableInfo : TableInfo) = {

    s"""
  case class ${tableInfo.tableRowName}(${printStandardClassColumns(tableInfo)}) extends KeyedEntity[${tableInfo.primaryKeyType}] {

    ${printConstructor(tableInfo)}

    ${printParents(tableInfo)}
    ${printChilds(tableInfo)}
  }
 """.trim()
  }

  def printStandardClassColumns(tableInfo : TableInfo) : String = {
    tableInfo.columns.map{ col =>
      if(standardColumnName(col.name).equals(tableInfo.primaryKeyName)) printPrimaryKeyColumn(col)
      else printColumn(col)
    }.mkString(", \n\t\t\t")
  }

  def printPrimaryKeyColumn(column : Column) = {
    s"""@Column("${column.name}") id : ${column.tpe}"""
  }

  def printColumn(column : Column) = {
    if(column.nullable) s"""@Column("${column.name}") ${standardColumnName(column.name)} : Option[${column.tpe}]"""
    else s"""@Column("${column.name}") ${standardColumnName(column.name)} : ${column.tpe}"""
  }

  def printParents(tableInfo : TableInfo) = {
    tableInfo.foreignKeys.map(fk => printParent(tableInfo, new TableInfo(model.tablesByName(fk.referencedTable)))).mkString("\n\n\t")
  }

  def printParent(referencingTableInfo : TableInfo, referencedTableInfo : TableInfo) = {

    val relationName = referencedTableInfo.nameCamelCasedUncapitalized + "To" + referencingTableInfo.nameCamelCased

    s"""lazy val ${referencedTableInfo.nameCamelCasedUncapitalized} = ${relationName}.right(this)"""
  }

  def printChilds(parentInfo : TableInfo) = {

   foreignKeyInfo.parentChildrenTablesInfo(parentInfo.table.name).map{ childInfo =>
    if(childInfo.isJunctionTable) printJunctionChild(parentInfo, childInfo)
    else printChild(childInfo, parentInfo)
   }.mkString("\n\n\t\t")
  }

  def printJunctionChild(parentTableInfo : TableInfo, junctionTableInfo : TableInfo) = {

    val (leftFk, rightFk) = (junctionTableInfo.foreignKeys(0), junctionTableInfo.foreignKeys(1))

    val leftTableInfo = new TableInfo(model.tablesByName(leftFk.referencedTable))

    val rightTableInfo = new TableInfo(model.tablesByName(rightFk.referencedTable))

    val relationName = leftTableInfo.nameCamelCasedUncapitalized + "To" + rightTableInfo.nameCamelCased

    if(leftTableInfo.name.equals(parentTableInfo.name)){
      s"""lazy val ${rightTableInfo.listName} = ${relationName}.left(this)"""
    } else {
      s"""lazy val ${leftTableInfo.listName} = ${relationName}.right(this)"""
    }
  }

  def printChild(referencingTableInfo : TableInfo, referencedTableInfo : TableInfo) = {

    val relationName = referencedTableInfo.nameCamelCasedUncapitalized + "To" + referencingTableInfo.nameCamelCased

    s"""lazy val ${referencingTableInfo.listName} = ${relationName}.left(this)"""
  }


  override def writeToFile(folder:String, pkg: String, fileName: String= "Tables.scala") {
      writeStringToFile(packageCode(pkg), folder, pkg, fileName)
    }

}

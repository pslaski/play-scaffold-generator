package generators.models.squeryl

import generators.utils._

import scala.slick.model.{Column, ForeignKey, Model}

object SchemaGenerator {
  def generate(outputFolder : String) = {

    val appConfig = AppConfigParser.getAppConfig

    val pkg = appConfig.modelsPackage

    val model = new ModelProvider(appConfig).model

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
        importCode("org.squeryl.dsl._"),
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

/*  def relations : String = {
    allTablesInfo.filter(_.foreignKeys.nonEmpty).map{ tableInfo =>
      if(tableInfo.isJunctionTable || tableInfo.isSimpleJunctionTable) printManyToManyRelation(tableInfo)
      else printOneToManyRelations(tableInfo)
    }.mkString("\n\n\t")
  }*/

  def relations = ""

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

    val relationName = referencedTableInfo.nameCamelCasedUncapitalized + "To" + referencingTableInfo.nameCamelCased + "By" + makeColumnsAndString(foreignKey.referencingColumns)

    val referencedColumn = standardColumnName(foreignKey.referencedColumns.head.name)

    val referencingColumn = standardColumnName(foreignKey.referencingColumns.head.name)

    val referencedValue = {
      if (foreignKey.referencingColumns.head.nullable) s"Option(a.${referencedColumn})"
      else "a." + referencedColumn
    }

        s"""
  val ${relationName} = {
    oneToManyRelation(${referencedTableInfo.queryObjectName}, ${referencingTableInfo.queryObjectName}).via((a, b) => ${referencedValue} === b.${referencingColumn})
  }
     """.trim()
  }

  def classes = {
    allTablesInfo.map{ tableInfo =>
      if(tableInfo.isSimpleJunctionTable) printSimpleJunctionClass(tableInfo)
      else if(tableInfo.isJunctionTable) printJunctionClass(tableInfo)
      else printStandardClass(tableInfo)
    }.mkString("\n\n\t")
  }

  def printSimpleJunctionClass(tableInfo : TableInfo) = {

/*    val fkTypes = tableInfo.foreignKeys.map(_.referencingColumns.head.tpe).mkString(", ")

    val fkColumns = tableInfo.foreignKeys.map( fk => standardColumnName(fk.referencingColumns.head.name)).mkString(", ")*/

    s"""
  case class ${tableInfo.tableRowName}(${printClassColumns(tableInfo)}) {

    ${printConstructor(tableInfo)}
  }
 """.trim()
  }

  def printJunctionClass(tableInfo : TableInfo) = {

    val keyedEntityType = makeCompositeKeyType(tableInfo.primaryKeyColumns)

    s"""
  case class ${tableInfo.tableRowName}(${printClassColumns(tableInfo)}) extends KeyedEntity[${keyedEntityType}] {

    ${printId(tableInfo)}

    ${printConstructor(tableInfo)}
  }
 """.trim()
  }

  def printClassColumns(tableInfo : TableInfo) : String = {
    val primaryKeyColumns = tableInfo.primaryKeyColumns

    tableInfo.columns.map{ col =>
      if(!tableInfo.isSimpleJunctionTable && primaryKeyColumns.length == 1 && col.name.equals(primaryKeyColumns.head.name)) printIdColumn(col)
      else printColumn(col)
    }.mkString(", \n\t\t\t")
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
      case "Boolean" => "false"
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

    val keyedEntityType = makeCompositeKeyType(tableInfo.primaryKeyColumns)

    s"""
  case class ${tableInfo.tableRowName}(${printClassColumns(tableInfo)}) extends KeyedEntity[${keyedEntityType}] {

    ${printId(tableInfo)}

    ${printConstructor(tableInfo)}

  }
 """.trim()
  }

  def printIdColumn(column : Column) = {
    s"""@Column("${column.name}") id : ${column.tpe}"""
  }

  def printColumn(column : Column) = {
    if(column.nullable) s"""@Column("${column.name}") ${standardColumnName(column.name)} : Option[${column.tpe}]"""
    else s"""@Column("${column.name}") ${standardColumnName(column.name)} : ${column.tpe}"""
  }

  def printId(tableInfo : TableInfo) : String = {

    val primaryKeyColumns = tableInfo.primaryKeyColumns

    if(primaryKeyColumns.length == 1){
      val column = primaryKeyColumns.head
      s"""def ${standardColumnName(column.name)} : ${column.tpe} = id"""
    }
    else {
      val compositeKeyType = makeCompositeKeyType(primaryKeyColumns)
      val compositeKey = makeCompositeKey(primaryKeyColumns)

      s"""override def id: ${compositeKeyType} = ${compositeKey}"""
    }
  }

  def printParents(tableInfo : TableInfo) = {
    tableInfo.foreignKeys.map(fk => printParent(tableInfo, new TableInfo(model.tablesByName(fk.referencedTable)), fk)).mkString("\n\n\t")
  }

  def printParent(referencingTableInfo : TableInfo, referencedTableInfo : TableInfo, foreignKey : ForeignKey) = {

    val parentName = referencedTableInfo.nameCamelCasedUncapitalized + "By" + makeColumnsAndString(foreignKey.referencingColumns)

    val relationName = referencedTableInfo.nameCamelCasedUncapitalized + "To" + referencingTableInfo.nameCamelCased + "By" + makeColumnsAndString(foreignKey.referencingColumns)

    s"""lazy val ${parentName} = ${relationName}.right(this)"""
  }

  def printChilds(parentInfo : TableInfo) = {

   foreignKeyInfo.foreignKeys.filter(_.referencedTable == parentInfo.table.name).map{fk =>
    val childInfo = new TableInfo(foreignKeyInfo.tablesByName(fk.referencingTable))

    if(childInfo.isJunctionTable || childInfo.isSimpleJunctionTable) printJunctionChild(parentInfo, childInfo)
    else printChild(childInfo, parentInfo, fk)
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

  def printChild(referencingTableInfo : TableInfo, referencedTableInfo : TableInfo, foreignKey : ForeignKey) = {

    val childName = referencingTableInfo.listName + "By" + makeColumnsAndString(foreignKey.referencingColumns)

    val relationName = referencedTableInfo.nameCamelCasedUncapitalized + "To" + referencingTableInfo.nameCamelCased + "By" + makeColumnsAndString(foreignKey.referencingColumns)

    s"""lazy val ${childName} = ${relationName}.left(this)"""
  }


  override def writeToFile(folder:String, pkg: String, fileName: String= "Tables.scala") {
      writeStringToFile(packageCode(pkg), folder, pkg, fileName)
    }

}

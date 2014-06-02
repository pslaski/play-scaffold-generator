package generators.slick.models

import generators.utils.{ModelProvider, Config}
import generators.slick.utils.{ForeignKeyInfo, TableInfo}

object TablesGenerator{
  def generate(config : Config, outputFolder : String) = {

    val pkg = config.modelsPackage

    val modelProvider = new ModelProvider(config)

    val slickDriverPath = modelProvider.slickDriverPath

    val mainModel = modelProvider.model
    val codegen = new scala.slick.model.codegen.SourceCodeGenerator(mainModel){


      override def Table = new Table(_){
        override def EntityType = new EntityType {
          override def code = {
            super.code + classBody
          }

          val mainTableInfo = new TableInfo(model)

          val foreignKeyInfo = new ForeignKeyInfo(mainModel)

          val tableChildren = foreignKeyInfo.parentChildrenTables(model.name)

          val joiningMethods = tableChildren.map{ table =>
            val tableInfo = new TableInfo(table)
            joiningMethod(tableInfo)
          }


         def classBody = {
           if(joiningMethods.nonEmpty) {
s"""
{
  ${indent(joiningMethods.mkString("\n"))}
}
""".trim
           }
           else ""
         }

          def joiningMethod(tableInfo : TableInfo) = {
            if(tableInfo.isJunctionTable) manyToManyJoinMethod(tableInfo)
            else oneToManyJoinMethod(tableInfo)
          }

          def oneToManyJoinMethod(tableInfo : TableInfo) = {

            val columns = {
              foreignKeyInfo.findForeignKeyBetween(mainTableInfo.table.name, tableInfo.table.name).map{ fk =>
                "row => " + ((fk.referencingColumns.map(_.name) zip fk.referencedColumns.map(_.name)).map{
                  case (lcol,rcol) => "row."+lcol + " === " + rcol
                }.mkString(" && "))
              }
            }

s"""
def ${tableInfo.listName} : List[${tableInfo.tableRowName}] = {
 ${tableInfo.queryObjectName}.filter(${columns.get}).list
}
""".trim
          }

          def manyToManyJoinMethod(tableInfo : TableInfo) = {

            val foreignKeyToFirstSide = tableInfo.foreignKeys.filter(_.referencedTable == mainTableInfo.table.name).head

            val columnsJoiningLeftWithJunctionTable = {
              "row => " + ((foreignKeyToFirstSide.referencingColumns.map(_.name) zip foreignKeyToFirstSide.referencedColumns.map(_.name)).map{
                case (lcol,rcol) => "row."+lcol + " === " + rcol
              }.mkString(" && "))
            }

            val foreignKeyToSecondSide = tableInfo.foreignKeys.filter(_.referencedTable != mainTableInfo.table.name).head

            val columnsJoiningJunctionWithRightTable = {
              "row => " + ((foreignKeyToSecondSide.referencingColumns.map(_.name) zip foreignKeyToSecondSide.referencedColumns.map(_.name)).map{
                case (lcol,rcol) => "row."+lcol + " === " + tableInfo.name +"." + rcol
              }.mkString(" && "))
            }

            val tableSecondSide = foreignKeyInfo.tablesByName(foreignKeyToSecondSide.referencedTable)

            val tableSecondSideInfo = new TableInfo(tableSecondSide)

s"""
def ${tableSecondSideInfo.listName} : List[${tableSecondSideInfo.tableRowName}] = {
 val query = for {
   ${tableInfo.name} <- ${tableInfo.queryObjectName}.filter(${columnsJoiningLeftWithJunctionTable})
   ${tableSecondSideInfo.listName} <- ${tableSecondSideInfo.queryObjectName}.filter(${columnsJoiningJunctionWithRightTable})
 } yield ${tableSecondSideInfo.listName}

 query.list
}
""".trim
          }
        }
      }

      // Generate auto-join conditions 1
      // append autojoin conditions to generated code
      override def code = {
        super.code + "\n\n" + s"""
/** implicit join conditions for auto joins */
object AutoJoins{
${indent(joins.mkString("\n"))}
}
""".trim()
      }
      // Generate auto-join conditions 2
      // assemble autojoin conditions
      val joins = tables.flatMap( _.foreignKeys.map{ foreignKey =>
        import foreignKey._
        val fkt = referencingTable.TableClass.name
        val pkt = referencedTable.TableClass.name
        val columns = referencingColumns.map(_.name) zip referencedColumns.map(_.name)
        s"""implicit def autojoin${fkt}${name.capitalize} = (left:${fkt},right:${pkt}) => """ +
        columns.map{
          case (lcol,rcol) => "left."+lcol + " === " + "right."+rcol
        }.mkString(" && ") + "\n" +
        s"""implicit def autojoin${fkt}${name.capitalize}Reverse = (left:${pkt},right:${fkt}) => """ +
        columns.map(_.swap).map{
          case (lcol,rcol) => "left."+lcol + " === " + "right."+rcol
        }.mkString(" && ")
      })
    }
    codegen.writeToFile(slickDriverPath, outputFolder, pkg)
  }
}
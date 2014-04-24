package org.modelgenerator

object TablesGenerator{
  def generate(args: Array[String]) = {

    val jdbcDriver = args(0)
    val url = args(1)
    val outputFolder = args(2)
    val pkg = args(3)
    val user = Option(args(4)) getOrElse("")
    val password = Option(args(5)) getOrElse("")
    
    val slickDriver = DriverLoader.slickDriver(jdbcDriver)
    val slickDriverPath = DriverLoader.slickDriverPath(jdbcDriver)

    val db = slickDriver.simple.Database.forURL(url,driver=jdbcDriver, user = user, password = password)
    val model = db.withSession(slickDriver.createModel(_))
    val codegen = new scala.slick.model.codegen.SourceCodeGenerator(model){

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
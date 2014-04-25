package org.modelgenerator

object TablesGenerator{
  def generate(config : Config, outputFolder : String) = {

    val jdbcDriver = config.jdbcDriver
    val url = config.url
    val pkg = config.modelsPackage
    val user = config.user
    val password = config.password
    
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
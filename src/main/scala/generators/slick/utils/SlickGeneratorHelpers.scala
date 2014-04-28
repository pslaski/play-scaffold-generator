package generators.slick.utils

import scala.slick.model.Column
import scala.slick.ast.ColumnOption.PrimaryKey
import generators.utils.StringUtils

trait SlickGeneratorHelpers extends StringUtils{

  def importCode(importPath : String) = "import " + importPath;

  val columns : Seq[Column]

  lazy val primaryKeyOpt = columns.find(_.options.contains(PrimaryKey))

  lazy val (primaryKeyName, primaryKeyType) = primaryKeyOpt match {
  	    case Some(col) => (col.name, col.tpe)
  	    case None => {
  	      val col = columns.head
  	      (col.name, col.tpe)
  	    }
  	  }

}
package generators.models.anorm

import generators.utils.OutputHelpers

object CustomColumnsGenerator extends OutputHelpers {

  def code : String = {
    s"""
object CustomColumns {

  import anorm._
  import anorm.Column._

  implicit val columnToSqlDate: Column[java.sql.Date] = nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case date: java.sql.Date => Right(date)
      case time: Long => Right(new java.sql.Date(time))
      case _ => Left(TypeDoesNotMatch(s"Cannot convert $$value: $${value.asInstanceOf[AnyRef].getClass} to Date for column $$qualified"))
    }
  }

  implicit val columnToTimestamp: Column[java.sql.Timestamp] = nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case timestamp: java.sql.Timestamp => Right(timestamp)
      case time: Long => Right(new java.sql.Timestamp(time))
      case _ => Left(TypeDoesNotMatch(s"Cannot convert $$value: $${value.asInstanceOf[AnyRef].getClass} to Date for column $$qualified"))
    }
  }

  implicit val columnToTime: Column[java.sql.Time] = nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case time: java.sql.Time => Right(time)
      case time: Long => Right(new java.sql.Time(time))
      case _ => Left(TypeDoesNotMatch(s"Cannot convert $$value: $${value.asInstanceOf[AnyRef].getClass} to Date for column $$qualified"))
    }
  }

}
    """.trim()
  }
  
  def indent(code: String): String = code
  
  override def writeToFile(folder:String, pkg: String, fileName: String="CustomColumns.scala"){
    super.writeToFile(folder, pkg, fileName)
  }
}
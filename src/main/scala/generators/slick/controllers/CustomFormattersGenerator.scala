package generators.slick.controllers

import generators.utils.OutputHelpers

object CustomFormattersGenerator extends OutputHelpers{

  def code : String = {
    s"""
import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.data.format.Formats._


object CustomFormats {

  private def parsing[T](parse: String => T, errMsg: String, errArgs: Seq[Any])(key: String, data: Map[String, String]): Either[Seq[FormError], T] = {
    stringFormat.bind(key, data).right.flatMap { s =>
      scala.util.control.Exception.allCatch[T]
        .either(parse(s))
        .left.map(e => Seq(FormError(key, errMsg, errArgs)))
    }
  }

  /**
   * Default formatter for the `Byte` type.
   */
  implicit def byteFormat: Formatter[Byte] = new Formatter[Byte] {

    override val format = Some(("format.numeric", Nil))

    def bind(key: String, data: Map[String, String]) =
      parsing(_.toByte, "error.number", Nil)(key, data)

    def unbind(key: String, value: Byte) = Map(key -> value.toString)
  }

  /**
   * Default formatter for the `Short` type.
   */
  implicit def shortFormat: Formatter[Short] = new Formatter[Short] {

    override val format = Some(("format.numeric", Nil))

    def bind(key: String, data: Map[String, String]) =
      parsing(_.toShort, "error.number", Nil)(key, data)

    def unbind(key: String, value: Short) = Map(key -> value.toString)
  }

  import java.sql.Time
  import java.sql.Timestamp
  import java.util.TimeZone

  /**
   * Formatter for the `java.sql.Time` type.
   *
   * @param pattern a date pattern as specified in `org.joda.time.format.DateTimeFormat`.
   * @param timeZone the `java.util.TimeZone` to use for parsing and formatting
   */
  def timeFormat(pattern: String, timeZone: TimeZone = TimeZone.getDefault): Formatter[java.sql.Time] = new Formatter[java.sql.Time] {

    val dateFormatter = dateFormat(pattern, timeZone)

    override val format = Some(("format.date", Seq(pattern)))

    def bind(key: String, data: Map[String, String]) = {
      dateFormatter.bind(key, data).right.map(d => new Time(d.getTime))
    }

    def unbind(key: String, value: java.sql.Time) = dateFormatter.unbind(key, value)
  }

  /**
   * Default formatter for `java.sql.Time` type with pattern `HH:mm`.
   */
  implicit val timeFormat: Formatter[java.sql.Time] = timeFormat("HH:mm")

  /**
   * Formatter for the `java.sql.Timestamp` type.
   *
   * @param pattern a date pattern as specified in `org.joda.time.format.DateTimeFormat`.
   * @param timeZone the `java.util.TimeZone` to use for parsing and formatting
   */
  def timestampFormat(pattern: String, timeZone: TimeZone = TimeZone.getDefault): Formatter[java.sql.Timestamp] = new Formatter[java.sql.Timestamp] {

    val dateFormatter = dateFormat(pattern, timeZone)

    override val format = Some(("format.date", Seq(pattern)))

    def bind(key: String, data: Map[String, String]) = {
      dateFormatter.bind(key, data).right.map(d => new Timestamp(d.getTime))
    }

    def unbind(key: String, value: java.sql.Timestamp) = dateFormatter.unbind(key, value)
  }

  /**
   * Default formatter for `java.sql.Timestamp` type with pattern `yyyy-MM-dd HH:mm:ssZ`.
   */
  implicit val timestampFormat: Formatter[java.sql.Timestamp] = timestampFormat("yyyy-MM-dd HH:mm:ssZ")

}

    """.trim()
  }

  def indent(code: String): String = code

  override def writeToFile(folder:String, pkg: String, fileName: String="CustomFormats.scala"){
    super.writeToFile(folder, pkg, fileName)
  }

}

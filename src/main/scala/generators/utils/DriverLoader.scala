package generators.utils

import scala.slick.driver._

object DriverLoader {
  
  val jdbcToSlickDriver = Map(
    "org.apache.derby.jdbc.EmbeddedDriver" -> DerbyDriver, 
    "org.h2.Driver" -> H2Driver, 
    "org.hsqldb.jdbcDriver" -> HsqldbDriver, 
    "com.mysql.jdbc.Driver" -> MySQLDriver, 
    "org.postgresql.Driver" -> PostgresDriver, 
    "org.sqlite.JDBC" -> SQLiteDriver)
  

  def slickDriver(jdbcDriver : String) : JdbcDriver = jdbcToSlickDriver.get(jdbcDriver) getOrElse(JdbcDriver)
  
  def slickDriverPath(jdbcDriver : String) : String = slickDriver(jdbcDriver).getClass().getCanonicalName().takeWhile(_ != '$')
}
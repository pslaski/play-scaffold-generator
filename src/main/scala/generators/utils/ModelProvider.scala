package generators.utils

import scala.slick.jdbc.meta.createModel

class ModelProvider(config : Config) {

  private val jdbcDriver = config.jdbcDriver
  private val url = config.url
  private val user = config.user
  private val password = config.password
  private val excluded = Seq("play_evolutions")

  lazy val slickDriver = DriverLoader.slickDriver(jdbcDriver)
  lazy val slickDriverPath = DriverLoader.slickDriverPath(jdbcDriver)

  lazy val db = slickDriver.simple.Database.forURL(url,driver=jdbcDriver, user = user, password = password)
  lazy val model = db.withSession { implicit session =>
    val tables = slickDriver.getTables.list.filterNot(t => excluded.exists(_.equalsIgnoreCase(t.name.name)))
    createModel( tables, slickDriver )
  }

}

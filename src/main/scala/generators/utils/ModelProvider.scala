package generators.utils

class ModelProvider(config : AppConfig) {

  private val jdbcDriver = config.jdbcDriver
  private val url = config.url
  private val user = config.user
  private val password = config.password
  private val excluded = Seq("play_evolutions")

  val slickDriver = config.slickDriver

  lazy val db = slickDriver.simple.Database.forURL(url,driver=jdbcDriver, user = user, password = password)
  lazy val model = db.withSession { implicit session =>
    val tables = slickDriver.defaultTables.filterNot(t => excluded.exists(_.equalsIgnoreCase(t.name.name)))
    slickDriver.createModel(Some(tables))
  }

}

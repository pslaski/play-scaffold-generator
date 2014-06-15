package generators.utils

class ModelProvider(config : Config) {

  private val jdbcDriver = config.jdbcDriver
  private val url = config.url
  private val user = config.user
  private val password = config.password

  lazy val slickDriver = DriverLoader.slickDriver(jdbcDriver)
  lazy val slickDriverPath = DriverLoader.slickDriverPath(jdbcDriver)

  lazy val db = slickDriver.simple.Database.forURL(url,driver=jdbcDriver, user = user, password = password)
  lazy val model = db.withSession(slickDriver.createModel(_))

}

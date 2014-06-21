package generators.utils

import com.typesafe.config.{ConfigFactory, ConfigException}
import scala.collection.JavaConversions._

trait ConfigUtils {

  def parseFile(configFile : java.io.File) = ConfigFactory.parseFile(configFile)

  def getOptionConfigList(key : String, config : com.typesafe.config.Config) : Option[List[com.typesafe.config.Config]] = {
    try{
      Some(config.getConfigList(key).toList)
    }
    catch {
      case missing : ConfigException.Missing => None
    }
  }

  def getOptionStringList(key : String, config : com.typesafe.config.Config) : Option[List[String]] = {
    try{
      Some(config.getStringList(key).toList)
    }
    catch {
      case missing : ConfigException.Missing => None
    }
  }

  def getOptionBoolean(key : String, config : com.typesafe.config.Config) : Option[Boolean] = {
    try{
      Some(config.getBoolean(key))
    }
    catch {
      case missing : ConfigException.Missing => None
    }
  }

  def getStringOrDefault(key : String, config : com.typesafe.config.Config, default : String = "") : String = {
 	  try{
 	    config.getString(key)
 	  }
 	  catch {
 	    case missing : ConfigException.Missing => default
 	  }
 	}

  def getOptionString(key : String, config : com.typesafe.config.Config) : Option[String] = {
 	  try{
 	    Some(config.getString(key))
 	  }
 	  catch {
 	    case missing : ConfigException.Missing => None
 	  }
 	}

}

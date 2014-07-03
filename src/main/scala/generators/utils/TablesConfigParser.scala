package generators.utils

import java.io.File

import com.typesafe.config.Config

object TablesConfigParser extends ConfigUtils{

  private var tablesConfigs : Option[List[TableConfig]] = None

  def parse(configFile : File) = {
    val config = parseFile(configFile)

    tablesConfigs = transformConfigsToTablesConfig(config)
  }
  
  def getTablesConfig = tablesConfigs

  private def transformConfigsToTablesConfig(config : Config) = getOptionConfigList("tables", config).map{
    configList => parseConfigList(configList)
  }

  def parseConfigList(configList : List[Config]) : List[TableConfig] = {

    val configsWithNames = configList.filter(getOptionString("table-name", _).isDefined)

    configsWithNames.map { cnfg =>
      new TableConfig(cnfg.getString("table-name"),
                       getOptionStringList("list-columns", cnfg),
                       getOptionStringList("select-columns", cnfg),
                       getOptionBoolean("is-junction-table", cnfg))
    }
  }

  def getTableConfigForName(name : String) : Option[TableConfig] = {
    tablesConfigs.map(_.find(_.tableName == name)).flatten
  }

}

case class TableConfig(tableName : String, listColumns : Option[List[String]], selectColumns : Option[List[String]], isJunctionTable : Option[Boolean])
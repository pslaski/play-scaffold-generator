package generators.utils

import java.io.File

object TablesConfigParser extends ConfigUtils{
	
	private var config : com.typesafe.config.Config = null

  private var tablesConfigs : Option[List[TableConfig]] = None

  def parse(configFile : File) = {
    config = parseFile(configFile)

    tablesConfigs = transformConfigsToTablesConfig
  }
  
  def getTablesConfig = tablesConfigs

  private def transformConfigsToTablesConfig = getOptionConfigList("tables", config).map{
    configList => parseConfigList(configList)
  }

  def parseConfigList(configList : List[com.typesafe.config.Config]) : List[TableConfig] = {

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
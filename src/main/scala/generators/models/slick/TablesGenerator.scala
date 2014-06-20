package generators.models.slick

import generators.utils.{Config, ModelProvider}

object TablesGenerator{
  def generate(config : Config, outputFolder : String) = {

    val pkg = config.modelsPackage

    val modelProvider = new ModelProvider(config)

    val slickDriverPath = modelProvider.slickDriverPath

    val mainModel = modelProvider.model
    val codegen = new scala.slick.model.codegen.SourceCodeGenerator(mainModel)
    codegen.writeToFile(slickDriverPath, outputFolder, pkg)
  }
}
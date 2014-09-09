package generators.views

import generators.utils.OutputHelpers

object TwitterBootstrapInputGenerator extends OutputHelpers{

  override def code: String = {
s"""
@(elements: views.html.helper.FieldElements)

@import play.api.i18n._
@import views.html.helper._

@**************************************************
* Generate input according twitter bootstrap rules *
**************************************************@

<div class="clearfix @elements.args.get('_class) @if(elements.hasErrors) {error}" id="@elements.args.get('_id).getOrElse(elements.id + "_field")">
    <label for="@elements.id">@elements.label(elements.lang)</label>
    <div class="input">
        @elements.input
        <span class="help-inline">@elements.errors(elements.lang).mkString(", ")</span>
        <span class="help-block">@elements.infos(elements.lang).mkString(", ")</span>
    </div>
</div>
 """.trim
  }

  override def indent(code: String): String = code

  override def writeToFile(folder:String, pkg: String, fileName: String="twitterBootstrapInput.scala.html") {
    writeStringToFile(code, folder, pkg, fileName)
  }
}

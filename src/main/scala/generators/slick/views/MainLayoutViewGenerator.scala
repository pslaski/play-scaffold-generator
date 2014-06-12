package generators.slick.views

import scala.slick.model.Model
import generators.utils.{StringUtils, OutputHelpers}
import generators.slick.utils.TableInfo

class MainLayoutViewGenerator(model : Model, appName : String) extends OutputHelpers with StringUtils{

  val tablesInfo = model.tables.map(new TableInfo(_))

  def menu = {
    s"""
<div class="collapse navbar-collapse">
	          <ul class="nav navbar-nav">
	            ${menuRows}
	          </ul>
	        </div>
     """.trim
  }

  def menuRows = {
    tablesInfo.map { tabInfo =>
      if(tabInfo.isJunctionTable) s"""<li><a href="@routes.${tabInfo.controllerName}.create">${tabInfo.nameCamelCased}</a></li>"""
      else s"""<li><a href="@routes.${tabInfo.controllerName}.list">${tabInfo.nameCamelCased}</a></li>"""
    }.mkString("\n")
  }

  def code = {
    s"""
@(title: String)(content: Html)

<!DOCTYPE html>

<html>
    <head>
        <meta charset="utf-8">
	    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	    <meta name="viewport" content="width=device-width, initial-scale=1">
	    <meta name="description" content="">
	    <meta name="author" content="">
	    <link rel="shortcut icon" type="image/png" href="@routes.Assets.at("images/favicon.png")">

	    <title>@title</title>

	    <!-- Bootstrap core CSS -->
        <link rel="stylesheet" media="screen" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css">
	    <!-- Bootstrap theme -->
        <link rel="stylesheet" media="screen" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap-theme.min.css">

	    <!-- Custom styles for this template -->
	    <link rel="stylesheet" media="screen" href="@routes.Assets.at("stylesheets/main.css")">

	    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
	    <!--[if lt IE 9]>
	      <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
	      <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
	    <![endif]-->
    </head>
    <body>
        <div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
	      <div class="container">
	        <div class="navbar-header">
	          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
	            <span class="sr-only">Toggle navigation</span>
	            <span class="icon-bar"></span>
	            <span class="icon-bar"></span>
	            <span class="icon-bar"></span>
	          </button>
	          <a class="navbar-brand" href="@routes.Application.index">${appName}</a>
	        </div>
	        ${menu}
	      </div>
	    </div>

	    <div class="container" role="main">
            <div class="row content-wrapper">
                <div class="col-lg-12">
                    @content
                </div>
            </div>
      </div>
        <script src="//code.jquery.com/jquery-1.11.0.min.js" type="text/javascript"></script>
        <script src="//netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js" type="text/javascript"></script>
    </body>
</html>
     """.trim
  }

  override def indent(code: String): String = code

  override def writeToFile(folder:String, pkg: String, fileName: String="main.scala.html") {
    writeStringToFile(code, folder, pkg, fileName)
  }
}

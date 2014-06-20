name := "play-scaffold-generator"

version := "1.0"

scalaVersion := "2.10.3"

sbtPlugin := true
       
libraryDependencies += "com.typesafe.slick" %% "slick" % "2.0.2"

libraryDependencies += "com.typesafe" % "config" % "1.2.1"

libraryDependencies += "org.postgresql" % "postgresql" % "9.3-1101-jdbc41"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.31"

import sbt._
import Keys._

object PlayScaffoldGenBuild extends Build {

  lazy val root = Project("play-scaffold-generator", file("."))
    .settings ( mainSettings:_* )
    .settings( sonatypePublishSettings:_*)

  // Main settings
  val mainSettings: Seq[Setting[_]] =
   Seq(
    organization := "com.github.pslaski",
    name := "play-scaffold-generator",
    version := "0.1.0",
    scalaVersion := "2.10.4",
    sbtPlugin := true,
    libraryDependencies ++= Seq(slick, slickCodegen, typesafeConfig, postgresqlJDBCDriver, mysqlJDBCDriver)
  )

  // Dependencies
  val slick = "com.typesafe.slick" %% "slick" % "2.1.0"

  val slickCodegen = "com.typesafe.slick" %% "slick-codegen" % "2.1.0"

  val typesafeConfig =  "com.typesafe" % "config" % "1.2.1"

  val postgresqlJDBCDriver =  "org.postgresql" % "postgresql" % "9.3-1101-jdbc41"

  val mysqlJDBCDriver =  "mysql" % "mysql-connector-java" % "5.1.31"


  // Sonatype settings
  def sonatypePublishSettings: Seq[Setting[_]] = Seq(
    publishMavenStyle := true,
    publishArtifact in Test := false,
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
      else                             Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    pomIncludeRepository := { _ => false },
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
    homepage := Some(url("https://github.com/pslaski/play-scaffold-generator")),
    scmInfo := Some(ScmInfo(url("https://github.com/pslaski/play-scaffold-generator"),"git@github.com:pslaski/play-scaffold-generator.git")),
    pomExtra := (
      <developers>
        <developer>
          <id>pslaski</id>
          <name>Paweł Śląski</name>
        </developer>
      </developers>
      )
  )

}
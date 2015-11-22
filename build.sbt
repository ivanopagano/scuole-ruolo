name := """scuole-ruolo"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

lazy val scalatest = "org.scalatest" %% "scalatest" % "2.2.1"
lazy val `scalatest+play` = "org.scalatestplus" %% "play" % "1.4.0-M3"
lazy val `scraper` = "net.ruippeixotog" %% "scala-scraper" % "0.1.2"


libraryDependencies ++= Seq(
  cache,
  ws,
  scraper,
  scalatest % Test,
  `scalatest+play` % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

herokuAppName in Compile := """scuole-ruolo"""

herokuJdkVersion in Compile := "1.8"

herokuProcessTypes in Compile := Map(
  "web" ->  "target/universal/stage/bin/scuole-ruolo -Dhttp.port=$PORT"
)

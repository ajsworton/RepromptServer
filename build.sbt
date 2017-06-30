name := """RepromptServer"""

organization := "com.reprompt"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test,
  "com.typesafe.play" %% "play-slick" % "3.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0",
  "mysql" % "mysql-connector-java" % "6.0.6",
  "org.mindrot" % "jbcrypt" % "0.4",
  ws
)

enablePlugins(JavaServerAppPackaging)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.reprompt.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.reprompt.binders._"

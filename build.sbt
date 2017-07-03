name := """RepromptServer"""

organization := "com.reprompt"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.2"

resolvers += "Atlassian Releases" at "https://maven.atlassian.com/public/"

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test,
  "com.typesafe.play" %% "play-slick" % "3.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0",
  "mysql" % "mysql-connector-java" % "6.0.6",
  "org.mindrot" % "jbcrypt" % "0.4",
  "com.google.code.findbugs" % "findbugs" % "3.0.1",
  "com.google.code.findbugs" % "jFormatString" % "2.0.1",
  "com.mohiva" %% "play-silhouette" % "5.0.0-RC1",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "5.0.0-RC1",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.0-RC1",
  "com.mohiva" %% "play-silhouette-persistence" % "5.0.0-RC1",
  "com.mohiva" %% "play-silhouette-testkit" % "5.0.0-RC1" % "test",
  ws
)

enablePlugins(JavaServerAppPackaging)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.reprompt.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.reprompt.binders._"

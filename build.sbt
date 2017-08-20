import com.typesafe.sbt.SbtScalariform._

import scalariform.formatter.preferences.{DanglingCloseParenthesis, DoubleIndentClassDeclaration, FormatXml, Preserve}

name := """RepromptServer"""

organization := "com.reprompt"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

resolvers += "Atlassian Releases" at "https://maven.atlassian.com/public/"
resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test,
  "com.typesafe.play" %% "play-slick" % "3.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0",
  "mysql" % "mysql-connector-java" % "6.0.6",
  "org.mindrot" % "jbcrypt" % "0.4",
  "com.iheart" %% "ficus" % "1.4.1",
  "com.google.code.findbugs" % "findbugs" % "3.0.1",
  "com.google.code.findbugs" % "jFormatString" % "2.0.1",
  "net.codingwell" %% "scala-guice" % "4.1.0",
  "com.mohiva" %% "play-silhouette" % "5.0.0-RC2",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "5.0.0-RC2",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.0-RC2",
  "com.typesafe.play" %% "play-mailer" % "6.0.0",
  "com.typesafe.play" %% "play-mailer-guice" % "6.0.0",
  "com.mohiva" %% "play-silhouette-persistence" % "5.0.0-RC2",
  "com.mohiva" %% "play-silhouette-testkit" % "5.0.0-RC2" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  ws
)

enablePlugins(JavaServerAppPackaging)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.reprompt.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.reprompt.binders._"


//********************************************************
// Scalariform settings
//********************************************************

defaultScalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(FormatXml, false)
  .setPreference(DoubleIndentClassDeclaration, false)
  .setPreference(DanglingCloseParenthesis, Preserve)


//testOptions in Test += Tests.Argument("-Dconfig.file=conf/application.test.conf")

//javaOptions in Test += "-Dconfig.file=conf/application.test.conf"
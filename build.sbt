import java.util.TimeZone

name := """RepromptServer"""

organization := "com.reprompt"

version := "1.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala, LauncherJarPlugin)

scalaVersion := "2.12.11"

resolvers += "Atlassian Releases" at "https://maven.atlassian.com/public/"
resolvers += Resolver.jcenterRepo

val silhouetteVersion = "7.0.0"
val playMailerVersion = "8.0.0"
val playSlickVersion  = "5.0.0"
val slickPgVersion    = "0.18.1"

libraryDependencies ++= Seq(
  guice,
  ws,
  "com.typesafe.play"        %% "play-slick"                      % playSlickVersion,
  "com.typesafe.play"        %% "play-slick-evolutions"           % playSlickVersion,
  "org.postgresql"           % "postgresql"                       % "42.2.11",
  "org.mindrot"              % "jbcrypt"                          % "0.4",
  "com.iheart"               %% "ficus"                           % "1.4.7",
  "net.codingwell"           %% "scala-guice"                     % "4.2.6",
  "commons-io"               % "commons-io"                       % "2.6",
  "com.typesafe.play"        %% "play-mailer"                     % playMailerVersion,
  "com.typesafe.play"        %% "play-mailer-guice"               % playMailerVersion,
  "com.mohiva"               %% "play-silhouette"                 % silhouetteVersion,
  "com.mohiva"               %% "play-silhouette-password-bcrypt" % silhouetteVersion,
  "com.mohiva"               %% "play-silhouette-crypto-jca"      % silhouetteVersion,
  "com.mohiva"               %% "play-silhouette-persistence"     % silhouetteVersion,
  "com.mohiva"               %% "play-silhouette-testkit"         % silhouetteVersion % "test",
  "org.mockito"              % "mockito-all"                      % "1.10.19" % "test",
  "org.scalatestplus.play"   %% "scalatestplus-play"              % "5.0.0" % "test",
  "com.opentable.components" % "otj-pg-embedded"                  % "0.13.3" % "test"
)

scalacOptions ++= Seq(
  "-encoding",
  "utf8", // Option and arguments on same line
  //  "-Xfatal-warnings",  // New lines for each options
  //  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)

enablePlugins(JavaServerAppPackaging)

enablePlugins(sbtdocker.DockerPlugin)

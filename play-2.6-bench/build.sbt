name := """play-2.6-bench"""
organization := "hochgi"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)//,PlayNettyServer).disablePlugins(PlayAkkaHttpServer)

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(guice,"org.lz4" % "lz4-java" % "1.4.0","com.typesafe.akka" %% "akka-parsing" % "10.0.10")
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
bashScriptExtraDefines += """export HOST_NAME=$(hostname)"""
fork in run := true

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "hochgi.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "hochgi.binders._"

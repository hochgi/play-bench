name := """play-2.5-bench"""
organization := "hochgi"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

libraryDependencies ++= Seq("org.lz4" % "lz4-java" % "1.4.0", "com.typesafe.akka" %% "akka-parsing" % "10.0.10")
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

fork in run := true

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "hochgi.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "hochgi.binders._"

import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.docker.Cmd

lazy val root = (project in file(".")).enablePlugins(JavaAppPackaging)

name := "weather-api"

version := "1.0.1"

scalaVersion := "2.12.2"

libraryDependencies ++= {
  val akkaVersion = "2.4.17"
  val akkaHttpVersion = "10.0.3"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "org.typelevel" %% "cats" % "0.9.0"
  )
}

/* Docker Settings */

dockerBaseImage := "frolvlad/alpine-oraclejdk8"
dockerCommands := dockerCommands.value.flatMap {
  case cmd@Cmd("FROM",_) => List(cmd, Cmd("RUN", "apk update && apk add bash"))
  case other => List(other)
}
packageName in Docker := name.value
dockerExposedPorts := Seq(9000)


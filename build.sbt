organization := "me.lessis"

name := "gist"

version  := "0.1.0-SNAPSHOT"

description := "gist it like you mean it"

scalaVersion := "2.9.2"

libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "core" % "0.9.0-beta1",
  "net.liftweb" % "lift-json_2.9.1" % "2.4"
)

resolvers += Classpaths.typesafeResolver

libraryDependencies <+= (sbtVersion)(
  "org.scala-sbt" %
   "launcher-interface" %
    _ % "provided")

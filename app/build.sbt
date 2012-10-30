organization := "me.lessis"

name := "gist-script"

version  := "0.1.1"

description := "conscript interface for gist"

resolvers += Classpaths.typesafeResolver

scalaVersion := "2.9.2"

libraryDependencies <+= (sbtVersion)(
  "org.scala-sbt" %
   "launcher-interface" %
    _ % "provided")

libraryDependencies += "org.slf4j" % "slf4j-jdk14" % "1.6.2"

publishTo := Some(Opts.resolver.sonatypeStaging)

licenses <<= version(v =>
      Seq("MIT" ->
          url("https://github.com/softprops/gist/blob/%s/LICENSE" format v)))

homepage :=
  Some(new java.net.URL("https://github.com/softprops/gist/"))

publishArtifact in Test := false

publishMavenStyle := true

pomExtra := (
  <scm>
    <url>git@github.com:softprops/gist.git</url>
    <connection>scm:git:git@github.com:softprops/gist.git</connection>
  </scm>
  <developers>
    <developer>
      <id>softprops</id>
      <name>Doug Tangren</name>
      <url>http://github.com/softprops</url>
    </developer>
  </developers>)

seq(lsSettings:_*)

LsKeys.tags in LsKeys.lsync := Seq("github", "gist", "conscript")

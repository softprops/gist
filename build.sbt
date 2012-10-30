organization := "me.lessis"

name := "gist"

version := "0.1.1-SNAPSHOT"

description := "gist it like you mean it"

libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "core" % "0.9.0-beta1",
  "net.liftweb" % "lift-json_2.9.1" % "2.4"
)

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

LsKeys.tags in LsKeys.lsync := Seq("github", "gist")

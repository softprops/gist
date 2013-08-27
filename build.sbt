organization := "me.lessis"

name := "gist"

version := "0.1.2-SNAPSHOT"

description := "gist it like you mean it"

homepage :=
  Some(new java.net.URL("https://github.com/softprops/gist/"))

licenses :=
      Seq("MIT" ->
          url("https://github.com/softprops/gist/blob/%s/LICENSE" format version.value))

resolvers += Classpaths.typesafeResolver

libraryDependencies += "me.lessis" %% "hubcat" % "0.1.1"

libraryDependencies +=
  ("org.scala-sbt" %
   "launcher-interface" %
    sbtVersion.value % "provided")

libraryDependencies += "org.slf4j" % "slf4j-jdk14" % "1.6.2"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.1.0"

scalacOptions += Opts.compile.deprecation

publishArtifact in Test := false

seq(bintraySettings:_*)

bintray.Keys.packageLabels in bintray.Keys.bintray := Seq("github", "gist")

seq(lsSettings:_*)

(LsKeys.tags in LsKeys.lsync) := Seq("github", "gist")

(externalResolvers in LsKeys.lsync) := (resolvers in bintray.Keys.bintray).value

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](version)

buildInfoPackage := "gist"

import sbt._

object Build extends sbt.Build {
  lazy val gist = Project("gist", file("."))
  lazy val script = Project("gist-script", file("app")) dependsOn(gist)
}

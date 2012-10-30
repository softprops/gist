package gist

import dispatch._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

trait Gists extends Serialization { self: Gist =>

  def mk(files: Seq[File], public: Boolean = true, desc: Option[String] = None) =
    self.http(self.withCredentials(self.api / "gists") << (
      compact(render(
        ("description" -> desc) ~ ("public" -> public) ~
        ("files" -> Map(files.map(f => (f.name, ("content" -> f.content))):_*))
      ))) > as.lift.Json)
      .either.right.map(fromGist)

  def user(name: String) =
    self.http(self.withCredentials(self.api / "users" / name / "gists").subject > as.lift.Json)
             .either.right.map(fromGists)

  def id(sha: String) =
    self.http(self.withCredentials(self.api / "gists" / sha).subject > as.lift.Json)
             .either.right.map(fromGist)

  def star(sha: String, set: Boolean = true) = {
    val base = self.withCredentials(self.api / "gists" / sha / "star")
    self.http((if (set) base.PUT else base.DELETE) > as.String).either
  }

  def rm(sha: String) =
    self.http(self.withCredentials(self.api.DELETE / "gists" / sha).subject > as.String).either

  def public =
    self.http(self.withCredentials(self.api / "gists" / "public").subject > as.lift.Json)
              .either.right.map(fromGists)

  def all =
    self.http(self.withCredentials(self.api / "gists").subject >  as.lift.Json)
             .either.right.map(fromGists)
}

package gist

import dispatch._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

case class File(name: String, content: String, size: Int = 0)

case class GistRef(id: String, url: String, htmlUrl: String, desc: String,
                   created: String, public: Boolean,
                   files: Seq[File] = Seq.empty[File],
                   author: String = "")

trait Serialization {
  def fromGist(js: JValue) =
    for {
      JObject(fields) <- js
      JField("id", JString(id)) <- fields
      JField("url", JString(url)) <- fields
      JField("html_url", JString(hurl)) <- fields
      JField("created_at", JString(created)) <- fields
      JField("public", JBool(public)) <- fields
      JField("files", JObject(files)) <- fields
      JField("user", JObject(user)) <- fields
    } yield {
      GistRef(id, url, hurl, "", created, public, files = for {
        JField(name, JObject(ffields)) <- files
        JField("content", JString(content)) <- ffields
        JField("size", JInt(size)) <- ffields
      } yield File(name, content, size.toInt), author = (for {
        JField("login", JString(login)) <- user
      } yield login).head)
    }

  def fromGists(js: JValue) =
    for {
      JArray(g) <- js
      JObject(fields) <- g
      JField("id", JString(id)) <- fields
      JField("url", JString(url)) <- fields
      JField("html_url", JString(hurl)) <- fields
      JField("description", JString(desc)) <- fields
      JField("created_at", JString(created)) <- fields
      JField("public", JBool(public)) <- fields
      JField("user", user) <- fields
    } yield {
      GistRef(id, url, hurl, desc, created, public, author = (for {
        JObject(ufields) <- user
        JField("login", JString(login)) <- ufields
      } yield login).head)
    }
}

trait Gists extends Serialization { self: Gist =>

  def mk(files: Seq[File], public: Boolean = true, desc: Option[String] = None) =
    self.http(self.withCredentials(self.api.POST / "gists").subject.setBody(compact(render(
      ("description" -> desc) ~ ("public" -> public) ~
      ("files" -> Map(files.map(f => (f.name, ("content" -> f.content))):_*))
    ))) > Json.parsed)
      .either.right.map(fromGist)

  def user(name: String) =
    self.http(self.withCredentials(self.api / "users" / name / "gists").subject > Json.parsed)
             .either.right.map(fromGists)

  def id(sha: String) =
    self.http(self.withCredentials(self.api / "gists" / sha).subject > Json.parsed)
             .either.right.map(fromGist)

  def visibility(sha: String, public: Boolean) =
    self.http(self.withCredentials(self.api.PATCH / "gists" / sha).subject.setBody(compact(render(
      ("public" -> public)
    )))  > Json.parsed).either

  def star(sha: String, set: Boolean = true) = {
    val base = self.withCredentials(self.api / "gists" / sha / "star")
    self.http((if (set) base.PUT else base.DELETE) > As.string).either
  }

  def rm(sha: String) =
    self.http(self.withCredentials(self.api.DELETE / "gists" / sha).subject > As.string).either

  def public =
    self.http(self.withCredentials(self.api / "gists" / "public").subject > Json.parsed)
              .either.right.map(fromGists)

  def all =
    self.http(self.withCredentials(self.api / "gists").subject >  /*Ok*/ Json.parsed)
             .either.right.map(fromGists)
}

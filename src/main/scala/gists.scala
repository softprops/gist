package gist

import dispatch._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

case class File(name: String, content: String, size: Int = 0)

case class GistRef(id: String, url: String, htmlUrl: String, desc: String,
                   created: String, public: Boolean,
                   files: Seq[File] = Seq.empty[File])

trait Serialization {
  def fromGist(js: JValue) =
    for {
      JObject(fields) <- js
      JField("id", JString(id)) <- fields
      JField("url", JString(url)) <- fields
      JField("html_url", JString(hurl)) <- fields
      JField("created_at", JString(created)) <- fields
      JField("public", JBool(public)) <- fields
      JField("files", files) <- fields
    } yield {
      GistRef(id, url, hurl, "", created, public, files = for {
        JObject(ffields) <- files
        JField(name, JObject(props)) <- ffields
        JField("content", JString(content)) <- props
        JField("size", JInt(size)) <- props
      } yield File(name, content, size.toInt))
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
    } yield {
      GistRef(id, url, hurl, desc, created, public)
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
    self.http(self.withCredentials(self.api / "users" / name / "gists").subject > /*Ok*/ Json.parsed)
             .either.right.map(fromGists)

  def id(sha: String) =
    self.http(self.withCredentials(self.api / "gists" / sha).subject > /*OK*/ Json.parsed)
             .either.right.map(fromGist)

  def all =
    self.http(self.withCredentials(self.api / "gists").subject >  /*Ok*/ Json.parsed)
             .either.right.map(fromGists)
}

package gist

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
      JField("created_at", JString(created)) <- fields
      JField("public", JBool(public)) <- fields
      JField("user", user) <- fields
      desc <- Some(fields.find({
        case JField("description", JString(_)) =>
          true
        case _ =>
          false
      }))
    } yield {
      GistRef(id, url, hurl, desc match {
        case Some(JField(_, JString(d))) => d
        case _ => ""
      }, created, public, author = (for {
        JObject(ufields) <- user
        JField("login", JString(login)) <- ufields
      } yield login).head)
    }
}

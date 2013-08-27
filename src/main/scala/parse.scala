package gist

import org.json4s._
import org.json4s.JsonDSL._

case class File(name: String, content: String, size: Int = 0)

case class GistRef(
  id: String,
  url: String,
  htmlUrl: String,
  desc: String,
  created: String,
  public: Boolean,
  files: Seq[File] = Seq.empty[File],
  author: String = "")

object Parse {
  def gist(js: JValue) =
    for {
      JObject(fields) <- js
      ("id", JString(id)) <- fields
      ("url", JString(url)) <- fields
      ("html_url", JString(hurl)) <- fields
      ("created_at", JString(created)) <- fields
      ("public", JBool(public)) <- fields
      ("files", JObject(files)) <- fields
      ("user", JObject(user)) <- fields
    } yield {
      GistRef(id, url, hurl, "", created, public, files = for {
        (name, JObject(ffields)) <- files
        ("content", JString(content)) <- ffields
        ("size", JInt(size)) <- ffields
      } yield File(name, content, size.toInt), author = (for {
        ("login", JString(login)) <- user
      } yield login).head)
    }

  def gists(js: JValue) =
    for {
      JArray(g) <- js
      JObject(fields) <- g
      ("id", JString(id)) <- fields
      ("url", JString(url)) <- fields
      ("html_url", JString(hurl)) <- fields
      ("created_at", JString(created)) <- fields
      ("public", JBool(public)) <- fields
      ("user", user) <- fields
      desc <- Some(fields.find({
        case ("description", JString(_)) =>
          true
        case _ =>
          false
      }))
    } yield {
      GistRef(id, url, hurl, desc match {
        case Some((_, JString(d))) => d
        case _ => ""
      }, created, public, author = (for {
        JObject(ufields) <- user
        ("login", JString(login)) <- ufields
      } yield login).head)
    }
}

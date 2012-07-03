package gist

import dispatch._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

object Authorize {
  val authorizations = :/("api.github.com").secure / "authorizations"
  val Access = "gh.auth.id"
  val AccessId = "gh.auth.token"
  val Login = "gh.auth.login"
}

trait Authorize { self: Gist =>
  import Authorize._
  
  def deauth =
    Config.get(AccessId) map { id =>
      self.http(self.withCredentials(authorizations.DELETE / id).subject > As.string)
      Config.properties { p =>
        val l = p.getProperty(Login)
        p.clear()
        l
      }
    }

  def auth(user: String, pass: String) =
    self.http (authorizations.POST.as_!(user, pass)
          .subject.setBody(compact(render(
            ("note" -> "dispatch_gist") ~
            ("note_url" -> "https://github.com/softprops/gist") ~
            ("scopes" -> ("gist" :: "user" :: Nil))))) > Json.parsed)
                 .either.right.map { js =>
                  (js \ "token", js \ "id") match {
                    case (JString(access), JInt(id)) =>
                      Config.properties { p =>
                        p.setProperty(AccessId, id.toString)
                        p.setProperty(Access, access)
                        p.setProperty(Login, user)
                      }
                      Right(access)
                    case _ => js \ "message" match {
                      case JString(err) => Left(err)
                      case _ => Left("Unexpected error communicating with github")
                    }
                  }
                }
}

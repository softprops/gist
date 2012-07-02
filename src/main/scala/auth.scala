package gist

import dispatch._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

object Authorize {
  val authorizations = :/("api.github.com").secure / "authorizations"
  val Access = "gh.access"
}

trait Authorize { self: Gist =>
  import Authorize._
  
  def auth(user: String, pass: String) =
    self.http (authorizations.POST.as_!(user, pass)
          .subject.setBody(compact(render(
            ("note" -> "dispatch_gist") ~
            ("note_url" -> "https://github.com/softprops/gist") ~
            ("scopes" -> ("gist" :: "user" :: Nil))))) > Json.parsed)
                 .either.right.map { js =>
                  js \ "token" match {
                    case JString(access) =>
                      Config.properties {
                        _.setProperty(Access, access)
                      }
                      Right(access)
                    case _ => js \ "message" match {
                      case JString(err) => Left(err)
                      case _ => Left("Unexpected error communicating with github")
                    }
                  }
                }
}

package gist

import dispatch._
import dispatch.Defaults._
import hubcat._
import org.json4s._

object Authorize {
  object properties {
    val AccessId = "gh.auth.id"
    val Access = "gh.auth.token"
    val Login = "gh.auth.login"
  }

  def client(http: Http) =
    Config.get(properties.Access).map(Client(_, http))

  def authorized =
    Config.get(properties.Login)

/*  def deauth =
    Config.get(AccessId) map { id =>
      AuthorizationClient().
      self.http(self.withCredentials(
        authorizations.DELETE / id).subject > as.String)()
      Config.properties { p =>
        val l = p.getProperty(Login)
        p.clear()
        l
      }
    }*/

  def apply(user: String, pass: String) = {
    for ( js <- AuthorizationClient(user, pass)
                .authorize
                .scopes("gist", "user")
                .url("https://github.com/softprops/gist")
                .note("dispatch-gist")(as.json4s.Json)) yield {
      (for {
        JObject(fs) <- js
        ("token", JString(token)) <- fs
        ("id", JInt(id)) <- fs
      } yield (id, token)).headOption.map {
        case (id, token) =>
          Config.properties { p =>
            p.setProperty(properties.AccessId, id.toString)
            p.setProperty(properties.Access, token)
            p.setProperty(properties.Login, user)
          }
          Right(token)
      }.getOrElse {
        (for { JObject(fs) <- js; ("message", JString(msg)) <- fs } yield msg)
        .headOption
        .map(Left(_))
        .getOrElse(Left("Unexpected error communicating with github"))
      }
    }
  }
}

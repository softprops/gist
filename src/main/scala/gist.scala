package gist

import dispatch._
import java.io.{ InputStream, OutputStream }
import java.util.Scanner

class Gist(val http: Http) extends Authorize
  with Credentials
  with Gists {
  protected def api = :/("api.github.com").secure
}

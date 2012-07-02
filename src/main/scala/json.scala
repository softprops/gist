package gist

import com.ning.http.client.Response

import net.liftweb.json._
import JsonDSL._

object Json {
  val parsed = dispatch.As.string.andThen(JsonParser.parse)
}

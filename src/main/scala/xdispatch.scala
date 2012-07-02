package gist

import dispatch._

trait DispatchExtras {
  def headers(req: RequestVerbs, headers: Map[String, String]) =
    (req /: headers)((r, h) => {
      h match { case (k, v) =>
          r.subject.addHeader(k, v)
          r
      }
    })
}

package gist

import dispatch._

trait Credentials extends DispatchExtras {
  def withCredentials(req: DefaultRequestVerbs) =
    Config.get(Authorize.Access)
                   .map(access => {
                     req.subject.addQueryParameter("access_token", access)
                     req
                   }).getOrElse(req)
  def authorized = Config.get(Authorize.Login)
}

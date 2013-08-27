package gist

import dispatch._
import dispatch.Defaults._
import hubcat._
import java.io.InputStream
import java.util.Scanner
import org.json4s._
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration.Inf
import xsbti.{ AppMain, AppConfiguration }

object Script {

  val AuthCredentials = """^(.+):(.+)""".r

  val http = new Http

  def cleanSha(shalike: String) =
    if (shalike.startsWith("https://gist.github.com/")) shalike.replaceFirst(
      "https://gist.github.com/", "")
    else shalike

  def authorized(f: Client => Future[Int]) =
     Authorize.client(http).map(f)
              .getOrElse(Future(err("login required")))

  def gist(fjs: Future[JValue]) =
    for (js <- fjs) yield Parse.gist(js)

  def gists(fjs: Future[JValue]) =
    for (js <- fjs) yield Parse.gists(js)

  sealed trait Cmd {
    def apply(): Future[Int]
  }

  case class Login(credentials: Option[(String, String)] = None) extends Cmd {

    def apply() = credentials.map {
      case (user, pass) => authorize(user, pass)
    }.getOrElse(ask)

    def ask =
      Console.readLine("enter login:password ") match {
        case AuthCredentials(user, pass) =>
          authorize(user, pass)
        case _ =>
          Future(err("expected format login:password"))
      }

    def authorize(user: String, pass: String) =
      for ( a <- Authorize(user, pass)) yield a.fold({ e =>
        err("error authenticating: %s" format e)
      }, {
        case _ => ok("authorized %s" format user)
      })
  }

  case object Whoami extends Cmd {
    def apply() = Future(
      ok(Authorize.authorized.getOrElse("nobody"))
    )
  }

  case class Cat(id: Option[String] = None) extends Cmd {
    def apply() =
      id.map { sha =>
        authorized { cli =>
          for (g <- gist(cli.gists.get(cleanSha(sha))(as.json4s.Json))) yield ok(
            g.map(cat).mkString("\n")
          )
        }
      }.getOrElse(Future(err("usage: gist cat <id>")))
  }

  case class Show(id: Option[String] = None) extends Cmd {
    def apply() = id.map { sha =>
      authorized { cli =>
        for (g <- gist(cli.gists.get(cleanSha(sha))(as.json4s.Json))) yield ok(
          g.map(show).mkString("\n")
        )
      }
    }.getOrElse(Future(err("usage: gist show <id>")))
  }

  case class Post(content: Option[String] = None, name: Option[String] = None, pub: Boolean = false) extends Cmd {
    def apply() = content.getOrElse(piped(System.in)) match {
      case empty if (empty.isEmpty) => Future(err("content (-c) required"))
      case cnt => authorized { cli =>
        val mk = (cli.gists.post.file(cnt, name.getOrElse("")) match {
          case g if (pub) => g.pub
          case g => g.secret
        })(as.json4s.Json)
        for ( g <- gist(mk)) yield ok(
          g.map(showOneline).mkString("\n")
        )
      }
    }
  }

  case class Rm(id: Option[String] = None) extends Cmd {
    def apply() = id.map { sha =>
        authorized { cli =>
          for (d <- cli.gists.delete(cleanSha(sha))(as.json4s.Json)) yield {
            ok("deleted %s" format sha)
          }
        }
    }.getOrElse(Future(err("usage: gist -d <id>")))
  }

  case class Star(id: Option[String] = None, set: Boolean = true) extends Cmd {
    def apply() = id.map { sha =>
      authorized { cli =>
        val id = cleanSha(sha)
        val star = cli.gists match {
          case g if (set) => g.star(id)
          case g => g.unstar(id)
        }
        for (s <- star(as.json4s.Json)) yield ok(
          "%s %s" format(if (set) "starred" else "unstarred", sha)
        )
      }
    }.getOrElse(Future(err("usage: gist star <id>")))
  }

  case class User(login: Option[String] = None) extends Cmd {
    def apply() = login.map { name =>
      authorized { cli =>
        for(gs <- gists(cli.gists.user(name)(as.json4s.Json))) yield ok(
          gs.map(show).mkString("\n")
        )
      }
    }.getOrElse(Future(err("usage: gist user <login>")))
  }

  case class Ls(everyone: Boolean = false) extends Cmd {
    def apply() = authorized { cli =>
      val xs = (cli.gists match {
        case g if (everyone) => g.everyone
        case g => g.owned
      })(as.json4s.Json)
      for (gs <- gists(xs)) yield ok(
        gs.map(show).mkString("\n")
      )
    }
  }

  case class Commands(command: Option[Cmd] = None) {
    def from(args: Array[String]) =
       new scopt.OptionParser[Commands]("gist") {
         head("gist", BuildInfo.version)

         cmd("login") text("authenticate with github") action {
           case (_, c) => c.copy(command = Some(Login()))
         } children(arg[String]("<user:password>") action {
           case (AuthCredentials(user, pass), c) => c.command match {
             case Some(l: Login) => c.copy(
               command = Some(l.copy(credentials = Some((user, pass))))
             )
           }
         })

         cmd("whoami") text("prints out the current user") action {
           case (_, c) => c.copy(command = Some(Whoami))
         }

         cmd("cat") text("prints the contents of a gist") action {
           case (_, c) => c.copy(command = Some(Cat()))
         } children(arg[String]("<sha>") text("id of gist") action {
           case (sha, c) => c.command match {
             case Some(cat: Cat) => c.copy(
               command = Some(cat.copy(id = Some(sha)))
             )
           }
         })

         cmd("show") text("lists the contents of a single gist") action {
           case (_, c) => c.copy(command = Some(Show()))
         } children(arg[String]("<sha>") text("id of gist") action {
           case (sha, c) => c.command match {
             case Some(show: Show) => c.copy(
               command = Some(show.copy(id = Some(sha)))
             )
           }
         })

         cmd("post") text("post gist content to github") action {
           case (_, c) => c.copy(command = Some(Post()))
         } children(opt[String]('c', "content") text("gist content") action {
           case (contents, c) => c.command match {
             case Some(p: Post) => c.copy(
               command = Some(p.copy(content = Some(contents)))
             )
           }
         }, opt[String]('n', "name") text("name of gist file") action {
           case (name, c) => c.command  match {
             case Some(p: Post) => c.copy(
               command = Some(p.copy(name = Some(name)))
             )
           }
         }, opt[Boolean]('p', "public") text("if true, the gist will be public") action {
           case (pub, c) => c.command match {
             case Some(p: Post) => c.copy(
               command = Some(p.copy(pub = pub))
             )
           }
         })

         cmd("ls") text("lists current users recent gists") action {
           case (_, c) => c.copy(command = Some(Ls()))
         } children(opt[Boolean]('e', "everyone") text("list of everyones gists") action {
           case (e, c) => c.command match {
             case Some(l: Ls) => c.copy(command = Some(l.copy(everyone = e)))
           }
         })

         cmd("rm") text("delete a gist") action {
           case (_, c) => c.copy(command = Some(Rm()))
         } children(arg[String]("<sha>") text("id of gist") action {
           case (sha, c) => c.command match {
             case Some(r: Rm) => c.copy(command = Some(r.copy(id = Some(sha))))
           }
         })

         cmd("star") text("star a gist") action {
           case (_, c) => c.copy(command = Some(Star()))
         } children(arg[String]("<sha>") text("id of gist") action {
           case (sha, c) => c.command match {
             case Some(s: Star) => c.copy(command = Some(s.copy(id = Some(sha))))
           }             
         })

         cmd("user") text("list gists posted by a given user") action {
           case (_, c) => c.copy(command = Some(User()))
         } children(arg[String]("<login>") text("login name of user") action {
           case (login, c) => c.command match {
             case Some(u: User) => c.copy(command = Some(u.copy(login = Some(login))))
           }
         })

       }.parse(args, this)
  }

  def apply(args: Array[String]): Future[Int] =
    Commands().from(args)
      .flatMap(_.command.map(_())).getOrElse(
        Future(err("invalid options"))
      )

  private def bold(txt: String) = 
    Console.BOLD + txt + Console.RESET

  private def showOneline(ref: GistRef) =
    ref.htmlUrl

  private def show(ref: GistRef) =
    "%s %s %s <%s> %s %s" format(if (ref.public) "+" else "-",
                         bold(ref.id),
                         ref.htmlUrl,
                         ref.author,
                         ref.desc,
                         ref.files.map(f => "\n * %s (%s)" format(f.name, f.size)).mkString("\n"))

  private def cat(ref: GistRef) =
    if (ref.files.isEmpty) "gist %s contained no files" format ref.id
    else ref.files.map(f => "%s\n\n%s".format(bold("* " + f.name), f.content)).mkString("\n\n")

  private def err(msg: Throwable): Int =
    err(msg.getMessage())

  private def err(msg: String): Int = {
    System.err.println("error: %s" format msg)
    1
  }

  private def ok(msg: String) = {
    println(msg)
    0
  }

  private def piped(in: InputStream): String = {
    @annotation.tailrec
    def check: String = in.available match {
      case n if (n > 0) =>
        @annotation.tailrec
        def consume(scan: Scanner, buf: StringBuffer): String = {
          if (scan.hasNextLine()) {
            buf.append(scan.nextLine)
            buf.append("\n")
            consume(scan, buf)
          } else buf.toString
        }
        consume(new Scanner(in), new StringBuffer())
      case _ =>
        Thread.sleep(100)
        check
    }
    check
  }
}

object Main {
  def main(args: Array[String]) {
    System.exit(Await.result(Script(args), Inf))
  }
}

class Script extends AppMain {
  def run(conf: AppConfiguration) =
    new Exit(Await.result(Script(conf.arguments), Inf))
}

class Exit(val code: Int) extends xsbti.Exit

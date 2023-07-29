import zio.*
import zio.http.*
import zio.http.netty.NettyConfig

object ZioHttpApp extends ZIOAppDefault:
  val PORT = 8080

  // Add route managers and middleware
  val httpProg =
    (HomeApp() ++ GreetingApp())
      @@ HttpAppMiddleware.debug

  // Server config
  // ZIO-HTTP server config
  val configLayer = ZLayer.succeed(Server.Config.default.port(PORT))

  // Define ZIO-http server
  val server: ZIO[Any, Throwable, Nothing] = Server
    .serve(httpProg)
    .provide( // Add required layers
      configLayer,
      ZLayer.succeed(NettyConfig.default),
      Server.customized,
    )

  val console = Console.printLine(s"Server started on http://localhost:${PORT}")

  // Run main application
  def run = console *> server

// These are our route managers
object HomeApp:
  def apply(): Http[Any, Nothing, Request, Response] =
    Http.collectZIO[Request] { case Method.GET -> Root =>
      ZIO.succeed(
        Response.redirect(URL(Root / "greet"))
      ) // GET /, redirect to /greet
    }

object GreetingApp:
  def apply(): Http[Any, Nothing, Request, Response] =
    Http.collectZIO[Request] {
      // GET /greet?name=:name or GET /greet?name=:name1&name=:name2
      case req @ (Method.GET -> Root / "greet")
          if req.url.queryParams.nonEmpty =>
        val names = req.url.queryParams.get("name").get.mkString(" and ")
        ZIO.succeed(Response.text(s"Hello $names!"))

      // GET /greet/:name
      case Method.GET -> Root / "greet" / name =>
        ZIO.succeed(Response.text(s"Hello $name!"))

      // GET /greet
      case Method.GET -> Root / "greet" =>
        // httpHitsMetric("GET", "/greet").increment.
        ZIO.succeed(Response.text("Hello World!"))
    }

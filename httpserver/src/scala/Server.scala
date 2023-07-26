import zio.*
import zio.http.*
import zio.http.netty.NettyConfig
import zio.http.netty.NettyConfig.LeakDetectionLevel
import zio.http.internal.middlewares.Cors.CorsConfig
import zio.http.Header.{AccessControlAllowMethods, AccessControlAllowOrigin, Origin}

object ZioHttpApp extends ZIOAppDefault:
  val PORT = 8080

  // Create CORS configuration
  val corsConfig: CorsConfig =
    CorsConfig(
      allowedOrigin = {
        case origin @ Origin.Value(_, host, _) if host == "dev" =>
          Some(AccessControlAllowOrigin.Specific(origin))
        case _ => None
      },
      allowedMethods = AccessControlAllowMethods(
        Method.PUT,
        Method.DELETE,
        Method.POST,
        Method.GET,
      ),
    )
  // Add route managers and middleware
  val httpProg =
    (HomeApp() ++ GreetingApp())
      @@ HttpAppMiddleware.cors(corsConfig)
      @@ HttpAppMiddleware.debug

  // Server config
  // ZIO-HTTP server config
  val configLayer =
    ZLayer.succeed(
      Server.Config.default
        .port(PORT)
    )

  val nettyConfigLayer = ZLayer.succeed(
    NettyConfig.default
      .leakDetection(LeakDetectionLevel.PARANOID)
      .maxThreads(2)
  )

  // Define ZIO-http server
  val server: ZIO[Any, Throwable, Nothing] = Server
    .serve(httpProg)
    .provide( // Add required layers
      configLayer,
      nettyConfigLayer,
      Server.customized,
    )

  val console = Console.printLine(s"Server started on http://localhost:${PORT}")
  def run     = console *> server

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

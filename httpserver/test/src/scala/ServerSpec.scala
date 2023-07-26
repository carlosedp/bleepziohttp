import zio.*
import zio.http.*
import zio.test.*

object MainSpec extends ZIOSpecDefault:

  val homeApp  = HomeApp()
  val greetApp = GreetingApp()

  def spec =
    suite("App Tests")(
      suite("Main backend application")(
        test("should show start message") {
          for
            _      <- ZioHttpApp.console
            output <- TestConsole.output
          yield assertTrue(output.head.contains("started"))
        },
        test("root route should redirect to /greet") {
          for
            response <- HomeApp().runZIO(Request.get(URL(Root)))
            body     <- response.body.asString
          yield assertTrue(
            response.status == Status.TemporaryRedirect,
            response
              .headers(Header.Location)
              .contains(Header.Location(URL(Root / "greet"))),
            body.isEmpty,
          )
        },
      ),
      suite("Greet backend application")(
        test("should greet world"):
          for
            response <- greetApp.runZIO(Request.get(URL(Root / "greet")))
            body     <- response.body.asString
          yield assertTrue(
            response.status == Status.Ok,
            body == "Hello World!",
          )
        ,
        test("should greet User if using path"):
          for
            response <- greetApp.runZIO(
                          Request.get(URL(Root / "greet" / "User"))
                        )
            body <- response.body.asString
          yield assertTrue(
            response.status == Status.Ok,
            body == "Hello User!",
          )
        ,
        test("should greet User if using query param"):
          for
            response <- greetApp.runZIO(
                          Request.get(
                            URL(Root / "greet", queryParams = QueryParams("name" -> "User"))
                          )
                        )
            body <- response.body.asString
          yield assertTrue(
            response.status == Status.Ok,
            body == "Hello User!",
          ),
      ),
    )

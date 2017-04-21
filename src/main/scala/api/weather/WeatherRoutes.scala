package api.weather

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import WeatherProtocol._
import WeatherApiSettings._

object WeatherRoutes {

  val routes =
    logRequestResult("weather-api") {
      pathPrefix("weather") {
        (post & entity(as[Data])) { data =>
          complete {
            WeatherService.weather(data.data).map[ToResponseMarshallable] {
              case Right(weather) => OK -> WeatherText(weather.toString)
              case Left(errorMessage) => BadRequest -> errorMessage
            }
          }
        }
      }
    }
}

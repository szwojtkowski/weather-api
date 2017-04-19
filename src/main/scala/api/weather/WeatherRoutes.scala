package api.weather

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol

object WeatherRoutes extends DefaultJsonProtocol {

  final case class Location(data: String)
  final case class Weather(description: String)

  implicit val locationFormat = jsonFormat1(Location.apply)
  implicit val weatherFormat = jsonFormat1(Weather.apply)

  val routes =
    logRequestResult("weather-api") {
      pathPrefix("weather") {
        (post & entity(as[Location])) { data =>
          complete {
            Weather(data.data)
          }
        }
      }
    }
}

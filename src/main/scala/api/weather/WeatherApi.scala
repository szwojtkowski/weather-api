package api.weather

import akka.http.scaladsl.Http
import api.weather.WeatherApiSettings._

import scala.util.{Failure, Success}

object WeatherApi extends App {
  Http().bindAndHandle(WeatherRoutes.routes, host, port).onComplete {
    case Success(s) => logger.info(s"Successfully bound to $host:$port.")
    case Failure(f) => logger.error(f, "Cannot bind.")
  }
}

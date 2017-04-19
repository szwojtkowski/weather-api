package api.weather

import akka.http.scaladsl.Http
import api.weather.WeatherApiSettings._

object WeatherApi extends App {
  Http().bindAndHandle(WeatherRoutes.routes, config.getString("http.interface"), config.getInt("http.port"))
}

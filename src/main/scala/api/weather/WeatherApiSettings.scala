package api.weather

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

trait WeatherApiSettings {
  val config = ConfigFactory.load()

  lazy val host = config.getString("http.host")
  lazy val port = config.getInt("http.port")

  lazy val apiKey = config.getString("services.accuweather.apikey")
  lazy val weatherServiceHost = config.getString("services.accuweather.host")
  lazy val weatherServicePort = config.getInt("services.accuweather.port")

  lazy val lang = config.getString("services.accuweather.language")
}

object WeatherApiSettings extends WeatherApiSettings {

  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val logger = Logging(system, getClass)
}

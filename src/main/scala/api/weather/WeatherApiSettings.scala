package api.weather

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

object WeatherApiSettings {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  lazy val apiKey = config.getString("services.accuweather.apikey")

  lazy val host = config.getString("services.accuweather.host")
  lazy val port = config.getInt("services.accuweather.port")

  lazy val lang = config.getString("services.accuweather.language")
}

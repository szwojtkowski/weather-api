package api.weather

import java.io.IOException

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{Uri, HttpResponse, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}
import api.weather.WeatherApiSettings._
import api.weather.location.LocationService
import cats.data.EitherT
import cats.instances.future._
import WeatherProtocol._

import scala.concurrent.Future

trait WeatherService {

  lazy val connectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection(weatherServiceHost, weatherServicePort)

  def weatherApiRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(connectionFlow).runWith(Sink.head)

  def createWeatherRequest(locationKey: String): HttpRequest = {
    val query = Uri.Query(("language", lang), ("apikey", apiKey))
    val uri = Uri(s"/currentconditions/v1/$locationKey.json").withQuery(query)
    RequestBuilding.Get(uri)
  }

  def weatherByLocationKey(key: String): Future[Either[String, Weather]] = {
    weatherApiRequest(createWeatherRequest(key)).flatMap(handleWeatherResponse)
  }

  def weather(location: String): Future[Either[String, Weather]] = {
    val locationWeather = for {
    location <- EitherT(LocationService.location(location))
    weather <- EitherT(weatherByLocationKey(location.key))
    } yield weather
    locationWeather.value
  }

  def handleWeatherResponse(response: HttpResponse): Future[Either[String, Weather]] = {
    response.status match {
      case OK => Unmarshal(response.entity).to[List[Weather]]
          .map(_.headOption.toRight("Cannot get weather for provided location."))
      case BadRequest => Future.successful(Left(s"Cannot get weather for provided location."))
      case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
        val error = s"Weather request failed with status code ${response.status} and entity $entity"
        logger.error(error)
        Future.failed(new IOException(error))
      }
    }
  }
}

object WeatherService extends WeatherService
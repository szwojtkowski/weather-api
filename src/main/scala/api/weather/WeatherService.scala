package api.weather

import java.io.IOException

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{Uri, HttpResponse, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}
import api.weather.WeatherApiSettings._
import api.weather.location.{Location, LocationService}
import cats.data.EitherT
import cats.instances.future._
import WeatherProtocol._

import scala.concurrent.Future

object WeatherService {

  lazy val connectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection(host, port)

  private def weatherRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(connectionFlow).runWith(Sink.head)

  def weatherByLocationKey(key: String): Future[Either[String, Weather]] = {
    val query = Uri.Query(("language", lang), ("apikey", apiKey))
    val uri = Uri(s"/currentconditions/v1/$key.json").withQuery(query)
    val request = RequestBuilding.Get(uri)
    weatherRequest(request).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[List[Weather]]
          .map(_.headOption.toRight("Cannot get weather for provided location."))
        case BadRequest => Future.successful(Left(s"Cannot get weather for provided location."))
        case _ => println(response); Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"Weather request failed with status code ${response.status} and entity $entity"
          logger.error(error)
          Future.failed(new IOException(error))
        }
      }
    }
  }

  def weather(location: String) = {
    val locationWeather = for {
      location <- EitherT(LocationService.location(location))
      weather <- EitherT(weatherByLocationKey(location.key))
    } yield weather
    locationWeather.value
  }

}

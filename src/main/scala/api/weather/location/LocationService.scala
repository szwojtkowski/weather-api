package api.weather.location

import java.io.IOException

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}
import api.weather.WeatherApiSettings._
import LocationProtocol._

import scala.concurrent.Future

object LocationService {

  lazy val connectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection(host, port)

  private def locationRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(connectionFlow).runWith(Sink.head)

  def location(location: String): Future[Either[String, Location]] = {
    val query = Uri.Query(("q", location), ("apikey", apiKey))
    val uri = Uri("/locations/v1/search").withQuery(query)
    val request = RequestBuilding.Get(uri)
    locationRequest(request).flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[List[Location]]
          .map(_.headOption.toRight("Cannot find provided location."))
        case BadRequest => Future.successful(Left(s"Cannot find provided location."))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"Location request failed with status code ${response.status} and entity $entity"
          logger.error(error)
          Future.failed(new IOException(error))
        }
      }
    }
  }
}

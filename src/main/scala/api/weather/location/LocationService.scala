package api.weather.location

import java.io.IOException

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}
import api.weather.WeatherApiSettings._
import LocationProtocol._

import scala.concurrent.Future

trait LocationService {

  lazy val connectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection(host, port)

  def locationApiRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(connectionFlow).runWith(Sink.head)

  def createLocationRequest(location: String): HttpRequest = {
    val query = Uri.Query(("q", location), ("apikey", apiKey))
    val uri = Uri("/locations/v1/search").withQuery(query)
    RequestBuilding.Get(uri)
  }

  def location(location: String): Future[Either[String, Location]] = {
    locationApiRequest(createLocationRequest(location)).flatMap(handleLocationResponse)
  }

  def handleLocationResponse(response: HttpResponse): Future[Either[String, Location]] = {
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

object LocationService extends LocationService

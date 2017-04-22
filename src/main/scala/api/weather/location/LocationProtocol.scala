package api.weather.location

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

trait LocationProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  implicit object LocationJsonFormat extends RootJsonFormat[Location] {
    def write(l: Location) = JsObject(
      "name" -> JsString(l.name),
      "key" -> JsString(l.key)
    )
    def read(value: JsValue) =
      value.asJsObject.getFields("LocalizedName", "Key") match {
        case Seq(JsString(name), JsString(key)) => Location(name, key)
        case _ => throw new DeserializationException("Expected location")
      }
  }
}

object LocationProtocol extends LocationProtocol

package api.weather

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._


object WeatherProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  implicit object TemperatureValueJsonFormat extends RootJsonFormat[TemperatureValue] {
    def write(t: TemperatureValue) = JsObject(
      "value" -> JsNumber(t.value),
      "unit" -> JsString(t.unit)
    )
    def read(value: JsValue) =
      value.asJsObject.getFields("Value", "Unit") match {
        case Seq(JsNumber(v), JsString(unit)) => TemperatureValue(v.toDouble, unit)
        case _ => throw new DeserializationException("Wrong temperature value format")
      }
  }

  implicit object TemperatureJsonFormat extends RootJsonFormat[Temperature] {
    def write(t: Temperature) = JsObject(
      "metric" -> t.metric.toJson,
      "imperial" -> t.imperial.toJson
    )
    def read(value: JsValue) =
      value.asJsObject.getFields("Metric", "Imperial") match {
        case Seq(metric, imperial) => Temperature(metric.convertTo[TemperatureValue], imperial.convertTo[TemperatureValue])
        case _ => throw new DeserializationException("Wrong temperature format")
      }
  }

  implicit object WeatherJsonFormat extends RootJsonFormat[Weather] {
    def write(w: Weather) = JsObject(
      "text" -> JsString(w.text),
      "temperature" -> w.temperature.toJson
    )
    def read(value: JsValue) =
      value.asJsObject.getFields("WeatherText", "Temperature") match {
        case Seq(JsString(text), temperature) => Weather(text, temperature.convertTo[Temperature])
        case _ => throw new DeserializationException("Wrong weather format")
      }
  }

  implicit val dataFormat = jsonFormat1(Data.apply)
  implicit val weatherTextFormat = jsonFormat1(WeatherText.apply)

}

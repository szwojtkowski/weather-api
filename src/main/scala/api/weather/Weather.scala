package api.weather

final case class Weather(text: String, temperature: Temperature) {
  override def toString = s"$text, ${temperature.metric.value} ${temperature.metric.unit}"
}

final case class WeatherText(response: String)

final case class Data(data: String)

final case class Temperature(metric: TemperatureValue, imperial: TemperatureValue)

final case class TemperatureValue(value: Double, unit: String)

package com.urdnot.iot.mpl3115a2

trait DataStructures {
  // ns:
  // 1600639700745000000
  // ms:
  // 1600639700745
  // sec:
  // 1600639700
  // {"inches": 29.732058476077967, "timestamp": 1600649072223, "altitude": 54.3125, "fahrenheit": 64.4, "kPascals": 100.67275, "celsius": 18.0}
  val msZeros: String = "000"
  val nsZeros: String = "000000000"
  final case class mpl3115a2(
                               timestamp: Option[Long],
                               inches: Option[Double],
                               altitude: Option[Double],
                               fahrenheit: Option[Double],
                               celsius: Option[Double],
                               kPascals: Option[Double]
                             ){
    def toInfluxString(host: String): Option[String] = {

      val measurement = s"""${mpl3115a2.this.getClass.getSimpleName},"""
      val tags: String = "sensor=mpl3115a2," +
        "host=" + host
      val fields: String = List(mpl3115a2.this.fahrenheit match {
        case Some(i) => "tempF=" + i
        case None => ""
      }, mpl3115a2.this.celsius match {
        case Some(i) => "tempC=" + i
        case None => ""
      }, mpl3115a2.this.inches match {
        case Some(i) => "inches=" + i
        case None => ""
      }, mpl3115a2.this.altitude match {
        case Some(i) => "altitude=" + i
        case None => ""
      }, mpl3115a2.this.kPascals match {
        case Some(i) => "kPascals=" + i
        case None => ""
      }
      ).mkString(",")
      val timestamp: String = mpl3115a2.this.timestamp.get.toString
      Some(measurement + tags + " " + fields + " " + timestamp)
    }

    private def round(d: Double) = BigDecimal(d).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    def celsiusToFahrenheit(d: Double) = round(d * 9 / 5 + 32)
    def fahrenheitToCelsius(d: Double) = round((d - 32) * 5 / 9)

  }
}




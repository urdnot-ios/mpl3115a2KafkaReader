package com.urdnot.iot.mpl3115a2

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object DataFormatter extends DataStructures {
  def prepareInflux(structuredData: DataProcessor.mpl3115a2): Future[Option[String]] = Future {
    structuredData.toInfluxString("pi-weather")
  }
}

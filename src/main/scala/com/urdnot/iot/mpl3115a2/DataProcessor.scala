package com.urdnot.iot.mpl3115a2

import com.typesafe.scalalogging.Logger
import io.circe.parser.parse
import io.circe.{Json, ParsingFailure}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DataProcessor extends DataStructures {
  val log: Logger = Logger("MPL3115A2_Processor")
  def parseRecord(record: Array[Byte], log: Logger): Future[Either[String, mpl3115a2]] = Future {
    val recordString = record.map(_.toChar).mkString
    val genericParse: Either[ParsingFailure, Json] = parse(recordString)
    import io.circe.optics.JsonPath._
    genericParse match {
      case Right(x) => x match {
        case x: Json => try {
          // {"inches": 29.732058476077967, "timestamp": 1600649072223, "altitude": 54.3125, "fahrenheit": 64.4, "kPascals": 100.67275, "celsius": 18.0}
          Right(mpl3115a2(
            timestamp = root.timestamp.long.getOption(x),
            inches = root.inches.double.getOption(x),
            altitude = root.altitude.double.getOption(x),
            fahrenheit = root.fahrenheit.double.getOption(x),
            celsius = root.celsius.double.getOption(x),
            kPascals = root.kPascals.double.getOption(x)))
        } catch {
          case e: Exception => Left("Unable to extract JSON: " + e.getMessage)
        }
        case _ => Left("I dunno what this is, but it's not a door message: " + x)
      }
      case Left(x) => Left(x.getMessage)
    }
  }
}

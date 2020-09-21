package com.urdnot.iot.mpl3115a2

import com.urdnot.iot.mpl3115a2.DataProcessor.{log, parseRecord}
import org.scalatest.flatspec.AsyncFlatSpec

class ParseJsonSuite extends AsyncFlatSpec with DataStructures {
  val validJsonMpl3115a2: Array[Byte] = """{"inches": 29.732058476077967, "timestamp": 1600649072223, "altitude": 54.3125, "fahrenheit": 64.4, "kPascals": 100.67275, "celsius": 18.0}""".getBytes
  val validJsonReply: mpl3115a2 = mpl3115a2(timestamp = Some(1600649072223L), inches = Some(29.732058476077967), altitude = Some(54.3125), fahrenheit = Some(64.4), celsius = Some(18.0), kPascals = Some(100.67275))
  val validInfluxReply: String = """mpl3115a2,sensor=mpl3115a2,host=pi-weather tempF=64.4,tempC=18.0,inches=29.732058476077967,altitude=54.3125,kPascals=100.67275 1600649072223"""

  behavior of "DataParser"
  it should "Correctly extract an object from the JSON " in {
    parseRecord(validJsonMpl3115a2, log).map { x =>
      assert(x == Right(validJsonReply))
    }
  }
  behavior of "DataFormatter"
  it should "prepare the influxdb update body " in {
    DataFormatter.prepareInflux(validJsonReply.asInstanceOf[DataProcessor.mpl3115a2]).map { x =>
      assert(x.get == validInfluxReply)
    }
  }
}

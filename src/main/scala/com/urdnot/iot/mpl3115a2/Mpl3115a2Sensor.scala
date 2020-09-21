package com.urdnot.iot.mpl3115a2

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model._
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.Config
import com.typesafe.scalalogging.{LazyLogging, Logger}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Mpl3115a2Sensor extends LazyLogging with DataStructures {

  implicit val system: ActorSystem = ActorSystem("barometer_processor")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = materializer.executionContext
  val log: Logger = Logger("MPL3115A2")

  val consumerConfig: Config = system.settings.config.getConfig("akka.kafka.consumer")
  val envConfig: Config = system.settings.config.getConfig("env")
  val bootstrapServers: String = consumerConfig.getString("kafka-clients.bootstrap.servers")
  val consumerSettings: ConsumerSettings[String, Array[Byte]] =
    ConsumerSettings(consumerConfig, new StringDeserializer, new ByteArrayDeserializer)
      .withBootstrapServers(bootstrapServers)

  val INFLUX_URL: String = "http://" + envConfig.getString("influx.host") + ":" + envConfig.getInt("influx.port") + envConfig.getString("influx.route")
  val INFLUX_USERNAME: String = envConfig.getString("influx.username")
  val INFLUX_PASSWORD: String = envConfig.getString("influx.password")
  val INFLUX_DB: String = envConfig.getString("influx.database")

  Consumer
    .plainSource(consumerSettings, Subscriptions.topics(envConfig.getString("kafka.topic")))
    .map { consumerRecord =>
      DataProcessor.parseRecord(consumerRecord.value(), log)
        .onComplete {
          case Success(x) => x match {
            case Right(valid) =>
              DataFormatter.prepareInflux(valid).onComplete {
                case Success(dataOptional) =>
                  dataOptional match {
                    case Some(data) =>
                      Http().singleRequest(HttpRequest(
                        method = HttpMethods.POST,
                        uri = Uri(INFLUX_URL).withQuery(
                          Query(
                            "bucket" -> INFLUX_DB,
                            "precision" -> "ms"
                          )
                        ),
                        headers = Seq(
                          Authorization(BasicHttpCredentials(INFLUX_USERNAME, INFLUX_PASSWORD))),
                        entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, data)
                      )).onComplete {
                        case Success(res) => res match {
                          case res if res.status.isFailure() => log.error(res.status.reason() + ": " + res.status.defaultMessage() + " Influx Message: " + res.headers(2))
                          case res if res.status.isSuccess() => log.debug(res.toString())
                          case _ => log.error("Influx failure: " + res)
                        }
                        case Failure(e) => log.error("Unable to connect to Influx: " + e.getMessage)
                      }
                    case _ => None
                  }
                case Failure(e) => log.error("Unable to connect to Influx: " + e.getMessage)
              }
              valid
            case Left(invalid) => log.error(invalid)
          }
          case Failure(exception) => log.error(exception.getMessage)
        }
    }
    .toMat(Sink.ignore)(DrainingControl.apply)
    .run()
}

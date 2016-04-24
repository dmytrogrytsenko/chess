package chess.settings

import chess.common.config.Settings
import com.typesafe.config.Config

import scala.concurrent.duration.FiniteDuration

case class EndpointSettings(config: Config) extends Settings {
  val interface = get[String]("interface")
  val port = get[Int]("port")
  val defaultTimeout = get[FiniteDuration]("defaultTimeout")
}


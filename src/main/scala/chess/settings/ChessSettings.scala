package chess.settings

import chess.common.config.Settings
import com.typesafe.config.Config

import scala.concurrent.duration.FiniteDuration

case class ChessSettings(config: Config) extends Settings {
  val mongo = get[MongoSettings]("mongo")
  val rest = get[RestSettings]("rest")
  val onlineUserTimeout = get[FiniteDuration]("onlineUserTimeout")
}

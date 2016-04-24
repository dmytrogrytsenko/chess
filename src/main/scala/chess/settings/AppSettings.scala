package chess.settings

import chess.common.config.Settings
import com.typesafe.config.Config

case class AppSettings(config: Config) extends Settings {
  val chess = get[ChessSettings]("chess")
}

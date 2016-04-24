package chess.settings

import chess.common.config.Settings
import com.typesafe.config.Config

case class RestSettings(config: Config) extends Settings {
  val internal = get[EndpointSettings]("internal")
  val external = get[EndpointSettings]("external")
}

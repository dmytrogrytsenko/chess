package chess.settings

import chess.common.config.Settings
import com.typesafe.config.Config

case class MongoSettings(config: Config) extends Settings {
  val hosts = get[List[String]]("hosts")
  val db = get[String]("db")
}

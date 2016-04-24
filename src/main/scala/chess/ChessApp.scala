package chess

import akka.actor.ActorSystem
import chess.settings.AppSettings
import com.typesafe.config.ConfigFactory
import chess.common.Messages.Start

import scala.io.StdIn._

object ChessApp extends App {
  val config = ConfigFactory.load()
  val system = ActorSystem("chess", config)
  val settings = AppSettings(config)
  system.actorOf(ChessGuardian.props(settings.chess), "chess") ! Start
  while (readLine() != "exit") { }
  system.terminate()
}


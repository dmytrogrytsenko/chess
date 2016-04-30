package chess.controllers

import akka.actor.Props
import chess.common.Messages.{Done, Start}
import chess.domain.Identifiers.Token
import chess.repositories.SessionRepository
import chess.repositories.SessionRepository.{SessionRemoved, RemoveSession}
import chess.rest.Controller

object LogoutController {
  def props(token: Token) = Props(classOf[LogoutController], token)
}

class LogoutController(token: Token) extends Controller {
  def receive = {
    case Start => SessionRepository.endpoint ! RemoveSession(token)
    case SessionRemoved(`token`) => complete(Done)
  }
}

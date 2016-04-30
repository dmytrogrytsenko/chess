package chess.controllers

import akka.actor.Props
import chess.common.Messages.Start
import chess.domain.Identifiers.UserId
import chess.domain._
import chess.repositories.UserRepository
import chess.repositories.UserRepository.{UserNotFoundById, UserFoundById, FindUserById}
import chess.rest.Controller
import chess.rest.Errors.Unauthorized

object GetProfileController {
  def props(userId: UserId) = Props(classOf[GetProfileController], userId)
}

class GetProfileController(userId: UserId) extends Controller {
  def receive = {
    case Start => UserRepository.endpoint ! FindUserById(userId)
    case UserFoundById(user) => complete(ProfileResult(UserData(user)))
    case UserNotFoundById(`userId`) => failure(Unauthorized.credentialsRejected)
  }
}
package chess.controllers

import akka.actor.Props
import akka.http.scaladsl.model.StatusCodes
import chess.common.Messages.Start
import chess.domain.{UserRegistrationResult, User, UserRegistrationData}
import chess.repositories.UserRepository
import chess.repositories.UserRepository.{UserAlreadyExists, UserAdded, AddUser}
import chess.rest.Controller
import chess.rest.Errors.Conflict

object RegisterUserController {
  def props(data: UserRegistrationData) = Props(classOf[RegisterUserController], data)
}
class RegisterUserController(data: UserRegistrationData) extends Controller {
  def receive = {
    case Start =>
      val user = User(data)
      UserRepository.endpoint ! AddUser(user)
      become(waitingForUserAdded(user))
  }

  def waitingForUserAdded(user: User): Receive = {
    case UserAdded(user.id) => complete(StatusCodes.OK -> UserRegistrationResult(user))
    case UserAlreadyExists(user.id) => failure(Conflict.userAlreadyExists(user.name))
  }
}

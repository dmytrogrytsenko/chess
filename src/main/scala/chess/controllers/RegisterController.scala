package chess.controllers

import akka.actor.Props
import chess.common.Messages.Start
import chess.domain.{User, UserData, RegisterResult, RegisterData}
import chess.repositories.UserRepository
import chess.repositories.UserRepository.{UserAlreadyExists, UserAdded, AddUser}
import chess.rest.Controller
import chess.rest.Errors.Conflict

object RegisterController {
  def props(data: RegisterData) = Props(classOf[RegisterController], data)
}

class RegisterController(data: RegisterData) extends Controller {
  def receive = {
    case Start =>
      val user = User(data)
      UserRepository.endpoint ! AddUser(user)
      become(waitingForUserAdded(user))
  }

  def waitingForUserAdded(user: User): Receive = {
    case UserAdded(user.id) => complete(RegisterResult(UserData(user)))
    case UserAlreadyExists(user.id) => failure(Conflict.userAlreadyExists(user.name))
  }
}

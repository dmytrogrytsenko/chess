package chess.controllers

import akka.actor.Props
import chess.common.Messages.Start
import chess.domain._
import chess.repositories.SessionRepository.{SessionAdded, AddSession}
import chess.repositories.{SessionRepository, UserRepository}
import chess.repositories.UserRepository.{UserNotFoundByName, UserFoundByName, FindUserByName}
import chess.rest.Controller
import chess.rest.Errors.Unauthorized

object LoginController {
  def props(data: LoginData) = Props(classOf[LoginController], data)
}

class LoginController(data: LoginData) extends Controller {
  def receive = {
    case Start =>
      UserRepository.endpoint ! FindUserByName(data.name)
      become(waitingForUserFound)
  }

  def waitingForUserFound: Receive = {
    case UserFoundByName(user) if user.password == data.password =>
      val session = Session.create(user.id)
      SessionRepository.endpoint ! AddSession(session)
      become(waitingForSessionAdded(user))
    case UserFoundByName(user) => failure(Unauthorized.credentialsRejected)
    case UserNotFoundByName(userName) => failure(Unauthorized.credentialsRejected)
  }

  def waitingForSessionAdded(user: User): Receive ={
    case SessionAdded(token) => complete(LoginResult(token, UserData(user)))
  }
}

package chess.controllers

import akka.actor.Props
import chess.common.Messages.Start
import chess.domain.GamesData
import chess.domain.Identifiers.UserId
import chess.game.Game
import chess.repositories.{GameRepository, UserRepository}
import chess.repositories.GameRepository.{FindGamesByUser, GamesFoundByUser}
import chess.repositories.UserRepository.{GetUsers, RetrievedUsers}
import chess.rest.Controller

object GetGamesController {
  def props(userId: UserId) =
    Props(classOf[GetGamesController], userId)
}

class GetGamesController(userId: UserId) extends Controller {
  def receive = {
    case Start =>
      GameRepository.endpoint ! FindGamesByUser(userId)
    case GamesFoundByUser(`userId`, games) =>
      val userIds = games.map(_.whitePlayerId).toSet ++ games.map(_.blackPlayerId).toSet
      UserRepository.endpoint ! GetUsers(userIds)
      become(waitingForUsers(games))
  }

  def waitingForUsers(games: List[Game]): Receive = {
    case RetrievedUsers(users) => complete(GamesData(games, users))
  }
}

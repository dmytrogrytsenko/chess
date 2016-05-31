package chess.controllers

import akka.actor.Props
import chess.common.Messages.Start
import chess.domain.{GameData, Game}
import chess.domain.Identifiers._
import chess.game.Game
import chess.repositories.UserRepository.{RetrievedUsers, GetUsers}
import chess.repositories.{UserRepository, GameRepository}
import chess.repositories.GameRepository.{GetGame, GameNotFound}
import chess.rest.Controller
import chess.rest.Errors.NotFound

object GetGameController {
  def props(userId: UserId, gameId: GameId, version: Option[Version]) =
    Props(classOf[GetGameController], userId, gameId, version)
}

class GetGameController(userId: UserId, gameId: GameId, version: Option[Version]) extends Controller {
  def receive = {
    case Start => GameRepository.endpoint ! GetGame(gameId)
    case GameNotFound(`gameId`) => failure(NotFound.gameNotFound)
    case game: Game if version.contains(game.version) => complete(None)
    case game: Game =>
      UserRepository.endpoint ! GetUsers(Set(game.whitePlayerId, game.blackPlayerId))
      become(waitingForUsers(game))
  }

  def waitingForUsers(game: Game): Receive = {
    case RetrievedUsers(users) => complete(Some(GameData(game, users)))
  }
}


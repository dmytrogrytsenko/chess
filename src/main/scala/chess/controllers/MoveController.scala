package chess.controllers

import akka.actor.Props
import chess.common.Messages.{Done, Start}
import chess.domain.Identifiers.{GameId, UserId}
import chess.game.{Game, PieceKind, Square}
import chess.repositories.GameRepository
import chess.repositories.GameRepository.{GameNotFound, GameUpdated, GetGame, UpdateGame}
import chess.rest.Controller
import chess.rest.Errors.{BadRequest, Forbidden, NotFound}

object MoveController {
  def props(userId: UserId, gameId: GameId, src: Square, dst: Square, promoted: Option[PieceKind]) =
    Props(classOf[MoveController], userId, gameId, src, dst, promoted)
}

class MoveController(userId: UserId,
                     gameId: GameId,
                     src: Square,
                     dst: Square,
                     promoted: Option[PieceKind]) extends Controller {
  def receive = {
    case Start => GameRepository.endpoint ! GetGame(gameId)
    case GameNotFound(`gameId`) => failure(NotFound.gameNotFound)
    case game: Game if game.whitePlayerId != userId && game.blackPlayerId != userId => failure(Forbidden.accessDenied)
    case game: Game =>
      game.movements.find(m => m.src == src && m.dst == dst && m.promoted.map(_.kind) == promoted) match {
        case Some(movement) =>
          val updatedGame = game.make(movement)
          GameRepository.endpoint ! UpdateGame(updatedGame)
        case None => failure(BadRequest.invalidMovement)
      }
    case GameUpdated(`gameId`) => complete(Done)
  }
}

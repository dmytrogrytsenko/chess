package chess.controllers

import akka.actor.Props
import chess.common.Messages.{Done, Start}
import chess.domain.Identifiers.{GameId, UserId}
import chess.game.{PieceKind, Square}
import chess.rest.Controller

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
    case Start => complete(Done)
  }
}
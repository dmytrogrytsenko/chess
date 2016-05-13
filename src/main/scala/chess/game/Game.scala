package chess.game

import chess.common._
import chess.domain.Identifiers._
import org.joda.time.DateTime
import chess.game.PieceMovements._

case class GameId(underlying: String)

object GameId {
  def generate = GameId(newUUID)
}

case class Game(id: GameId,
                version: Version,
                startTime: DateTime,
                board: Board,
                movingPlayer: PieceColor,
                initials: Set[Square],
                history: List[Movement]) {

  lazy val movements = validMovements(this, movingPlayer)
  lazy val check = board.king(movingPlayer).exists(underAttack(this, _))
  lazy val checkmate: Boolean = check && movements.isEmpty
  lazy val stalemate: Boolean = !check && movements.isEmpty

  def make(movement: Movement): Game = copy(
    version = version.next,
    board = board.make(movement),
    movingPlayer = movingPlayer.opposite,
    initials = initials - movement.src,
    history = history :+ movement)
}

object Game {
  def create: Game = new Game(
    id = GameId.generate,
    version = Version.initial,
    startTime = DateTime.now,
    board = Board.initial,
    movingPlayer = White,
    initials = Board.initial.squares.keySet,
    history = List.empty)
}


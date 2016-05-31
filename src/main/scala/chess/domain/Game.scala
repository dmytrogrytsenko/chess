package chess.domain

import chess.common._
import chess.domain.Identifiers._
import chess.game.Game
import org.joda.time.DateTime

case class GameData(id: GameId,
                    version: Version,
                    whitePlayer: UserData,
                    blackPlayer: UserData,
                    startTime: DateTime)

object GameData {
  def apply(game: Game, users: List[User]): GameData =
    GameData(
      id = game.id,
      version = game.version,
      whitePlayer = users
        .find(_.id == game.whitePlayerId)
        .getOrElse(User.unknown(game.whitePlayerId))
        .pipe(UserData.apply),
      blackPlayer = users
        .find(_.id == game.blackPlayerId)
        .getOrElse(User.unknown(game.blackPlayerId))
        .pipe(UserData.apply),
      startTime = game.startTime)
}

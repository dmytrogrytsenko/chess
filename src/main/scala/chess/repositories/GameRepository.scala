package chess.repositories

import chess.common._
import chess.common.Messages.Start
import chess.common.actors.{BaseActor, NodeSingleton1}
import chess.domain.Identifiers.{GameId, UserId}
import chess.game.Game
import chess.mongo.GameCollection
import reactivemongo.api.DB

object GameRepository extends NodeSingleton1[GameRepository, DB] {
  case class GetGame(id: GameId)
  case class GameNotFound(id: GameId)
  case class FindGamesByUser(userId: UserId)
  case class GamesFoundByUser(userId: UserId, games: List[Game])
  case class AddGame(game: Game)
  case class GameAdded(gameId: GameId)
  case class UpdateGame(game: Game)
  case class GameUpdated(gameId: GameId)
}

class GameRepository(implicit val db: DB) extends BaseActor {
  import GameCollection._
  import GameRepository._
  import context.dispatcher

  def receive = {
    case Start => ensureIndexes

    case GetGame(id) =>
      get(id) map {
        case Some(game) => game
        case None => GameNotFound(id)
      } pipeTo sender()

    case FindGamesByUser(userId) =>
      getGames(userId).map(games => GamesFoundByUser(userId, games)).pipeTo(sender())

    case AddGame(game) =>
      add(game).map(_ => GameAdded(game.id)).pipeTo(sender())

    case UpdateGame(game) =>
      update(game.id, game).map(_ => GameUpdated(game.id)).pipeTo(sender())

  }
}

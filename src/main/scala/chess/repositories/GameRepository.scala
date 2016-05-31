package chess.repositories

import chess.common._
import chess.common.Messages.Start
import chess.common.actors.{BaseActor, NodeSingleton1}
import chess.domain.Identifiers.GameId
import chess.mongo.GameCollection
import reactivemongo.api.DB

object GameRepository extends NodeSingleton1[UserRepository, DB] {
  case class GetGame(id: GameId)
  case class GameNotFound(id: GameId)
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
  }
}

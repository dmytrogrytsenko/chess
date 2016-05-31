package chess.mongo

import chess.common.mongo.MongoCollection
import chess.domain.Identifiers._
import chess.game._
import org.joda.time.DateTime
import reactivemongo.bson._
import reactivemongo.extensions.dao.Handlers.BSONDateTimeHandler

object GameCollection extends MongoCollection[GameId, Game] {
  val name = "games"

  import VersionCollection.VersionReader
  import VersionCollection.VersionWriter

  import UserCollection.UserIdReader
  import UserCollection.UserIdWriter

  implicit object GameIdReader extends BSONReader[BSONString, GameId] {
    def read(bson: BSONString): GameId = bson.value.toGameId
  }

  implicit object GameIdWriter extends BSONWriter[GameId, BSONString] {
    def write(value: GameId): BSONString = BSONString(value)
  }

  implicit object SquareReader extends BSONReader[BSONString, Square] {
    def read(bson: BSONString): Square = Square(bson.value)
  }

  implicit object SquareWriter extends BSONWriter[Square, BSONString] {
    def write(value: Square): BSONString = BSONString(value.name)
  }

  implicit object PieceReader extends BSONReader[BSONString, Piece] {
    def read(bson: BSONString): Piece = Piece(bson.value)
  }

  implicit object PieceWriter extends BSONWriter[Piece, BSONString] {
    def write(value: Piece): BSONString = BSONString(value.name)
  }

  implicit object BoardWriter extends BSONDocumentWriter[Board] {
    def write(value: Board): BSONDocument = value.squares
      .map { case (square, piece) => $doc(square.name -> piece.name) }
      .foldLeft($empty)(_ ++ _)
  }

  implicit object BoardReader extends BSONDocumentReader[Board] {
    def read(doc: BSONDocument): Board = ???
  }

  implicit object GameWriter extends BSONDocumentWriter[Game] {
    def write(value: Game) = $doc(
      "_id" -> value.id,
      "version" -> value.version,
      "whitePlayerId" -> value.whitePlayerId,
      "blackPlayerId" -> value.blackPlayerId,
      "startTime" -> value.startTime,
      "board" -> value.board,
      "movingPlayer" -> value.movingPlayer,
      "initials" -> value.initials,
      "history" -> value.history)
  }

  implicit object GameReader extends BSONDocumentReader[Game] {
    def read(doc: BSONDocument) = Game(
      id = doc.getAs[GameId]("_id").get,
      version = doc.getAs[Version]("version").get,
      whitePlayerId = doc.getAs[UserId]("whitePlayerId").get,
      blackPlayerId = doc.getAs[UserId]("blackPlayerId").get,
      startTime = doc.getAs[DateTime]("startTime").get,
      board = doc.getAs[Board]("board").get,
      movingPlayer = doc.getAs[PieceColor]("movingPlayer").get,
      initials = doc.getAs[Set[Square]]("initials").get,
      history = doc.getAs[List[Movement]]("history").get
    )
  }
}

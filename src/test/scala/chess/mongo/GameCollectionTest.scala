package chess.mongo

import chess.TestBase
import chess.common._
import chess.domain.Identifiers.{GameId, UserId}
import chess.game._

import scala.concurrent.ExecutionContext.Implicits.global

class GameCollectionTest extends TestBase {

  import GameCollection._

  behavior of "GameIdReader and GameIdWriter"

  it should "read and write GameId correctly" in {
    //arrange
    val value = GameId.generate()
    //act
    val result = GameIdReader.read(GameIdWriter.write(value))
    //assert
    result shouldBe value
  }

  behavior of "SquareReader and SquareWriter"

  it should "read and write Square correctly" in {
    //arrange
    val value = Square.all.toSet.randomValue
    //act
    val result = SquareReader.read(SquareWriter.write(value))
    //assert
    result shouldBe value
  }

  behavior of "PieceColorReader and PieceColorWriter"

  it should "read and write PieceColor correctly" in {
    //arrange
    val value = Set(White, Black).randomValue
    //act
    val result = PieceColorReader.read(PieceColorWriter.write(value))
    //assert
    result shouldBe value
  }

  behavior of "PieceReader and PieceWriter"

  it should "read and write Piece correctly" in {
    //arrange
    val value = buildPiece()
    //act
    val result = PieceReader.read(PieceWriter.write(value))
    //assert
    result shouldBe value
  }

  behavior of "MovementKindReader and MovementKindWriter"

  it should "read and write MovementKind correctly" in {
    //arrange
    val value = MovementKind.all.randomValue
    //act
    val result = MovementKindReader.read(MovementKindWriter.write(value))
    //assert
    result shouldBe value
  }

  behavior of "CastlingKindReader and CastlingKindWriter"

  it should "read and write CastlingKind correctly" in {
    //arrange
    val value = CastlingKind.all.randomValue
    //act
    val result = CastlingKindReader.read(CastlingKindWriter.write(value))
    //assert
    result shouldBe value
  }

  behavior of "BoardReader and BoardWriter"

  it should "read and write empty Board correctly" in {
    //arrange
    val value = Board.empty
    //act
    val result = BoardReader.read(BoardWriter.write(value))
    //assert
    result shouldBe value
  }

  it should "read and write initial Board correctly" in {
    //arrange
    val value = Board.initial
    //act
    val result = BoardReader.read(BoardWriter.write(value))
    //assert
    result shouldBe value
  }

  behavior of "MovementReader and MovementWriter"

  it should "read and write Movement correctly" in {
    //arrange
    val value = buildMovement()
    //act
    val result = MovementReader.read(MovementWriter.write(value))
    //assert
    result shouldBe value
  }

  behavior of "getGames"

  it should "return user's games" in {
    //arrange
    val userId = UserId.generate()
    val game1 = Mongo.addGame(whitePlayerId = userId)
    val game2 = Mongo.addGame(blackPlayerId = userId)
    Mongo.addGame()
    //act
    val result = getGames(userId).await
    //assert
    result should contain allOf(game1, game2)
  }
}

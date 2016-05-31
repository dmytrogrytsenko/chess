package chess.game

import chess.game.Pieces._
import chess.TestBase
import chess.domain.Identifiers._
import org.joda.time.DateTime

import scala.concurrent.duration._

class GameTest extends TestBase {

  behavior of "Game.create"

  it should "create new game" in {
    //act
    val result = Game.create(UserId.generate(), UserId.generate())
    //assert
    result.id.length should be > 0
    result.version shouldBe Version.initial
    result.startTime shouldBeInRange DateTime.now +- 2.seconds
    result.board shouldBe Board.initial
    result.movingPlayer shouldBe White
    result.initials shouldBe Set(1, 2, 7, 8).map(Rank(_).squares).reduce(_ ++ _).toSet
    result.history shouldBe List.empty
  }

  behavior of "Game.check"

  it should "return false for new game" in {
    //act
    val result = Game.create(UserId.generate(), UserId.generate()).check
    //assert
    result shouldBe false
  }

  it should "return true if king in check from queen" in {
    //arrange
    val game = Game
      .create(UserId.generate(), UserId.generate())
      .copy(board = Board.empty.copy(squares = Map(
        Square("e1") -> WhiteKing,
        Square("b4") -> BlackQueen)))
    //act
    val result = game.check
    //assert
    result shouldBe true
  }
}

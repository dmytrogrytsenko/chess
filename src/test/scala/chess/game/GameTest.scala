package chess.game

import chess.game.Pieces._
import chess.TestHelpers
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._

class GameTest extends FlatSpec with Matchers with TestHelpers {

  behavior of "Game.create"

  it should "create new game" in {
    //act
    val result = Game.create
    //assert
    result.id.underlying.length should be > 0
    result.version shouldBe Version.initial
    result.startTime shouldBeInRange DateTime.now +- 1.second
    result.board shouldBe Board.initial
    result.movingPlayer shouldBe White
    result.initials shouldBe Set(1, 2, 7, 8).map(Rank(_).squares).reduce(_ ++ _).toSet
    result.history shouldBe List.empty
  }

  behavior of "Game.check"

  it should "return false for new game" in {
    //act
    val result = Game.create.check
    //assert
    result shouldBe false
  }

  it should "return true if king in check from queen" in {
    //arrange
    val game = Game.create.copy(board = Board.empty.copy(squares = Map(
      Square("e1") -> WhiteKing,
      Square("b4") -> BlackQueen)))
    //act
    val result = game.check
    //assert
    result shouldBe true
  }
}

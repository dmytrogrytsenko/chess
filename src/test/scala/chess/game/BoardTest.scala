package chess.game

import chess.game.Pieces._
import org.scalatest.{Matchers, FlatSpec}

class BoardTest extends FlatSpec with Matchers {

  behavior of "Board.empty"

  it should "return empty board" in {
    //act
    val result = Board.empty
    //assert
    result should be (Board(Map.empty))
  }

  behavior of "Board.initial"

  it should "return initial board" in {
    //act
    val result = Board.initial
    //assert
    result should be (Board(whitePieces ++ blackPieces))
  }

  behavior of "Board.byColor"

  it should "return empty color for empty board" in {
    //act
    val result = Board.empty.byColor
    //assert
    result should be (Map(White -> Map.empty, Black -> Map.empty))
  }

  it should "return pieces by color" in {
    //act
    val result = Board.initial.byColor
    //assert
    result should be (Map(White -> whitePieces, Black -> blackPieces))
  }

  behavior of "Board.king"

  it should "return None if no king on board" in {
    //act
    val result = Board.empty.king
    //assert
    result should be (Map(White -> None, Black -> None))
  }

  it should "return kings square by color" in {
    //act
    val result = Board.initial.king
    //assert
    result should be (Map(White -> Some(Square("e1")), Black -> Some(Square("e8"))))
  }

  val whitePieces = Map(
    Square("a1") -> WhiteRook,
    Square("b1") -> WhiteKnight,
    Square("c1") -> WhiteBishop,
    Square("d1") -> WhiteQueen,
    Square("e1") -> WhiteKing,
    Square("f1") -> WhiteBishop,
    Square("g1") -> WhiteKnight,
    Square("h1") -> WhiteRook,

    Square("a2") -> WhitePawn,
    Square("b2") -> WhitePawn,
    Square("c2") -> WhitePawn,
    Square("d2") -> WhitePawn,
    Square("e2") -> WhitePawn,
    Square("f2") -> WhitePawn,
    Square("g2") -> WhitePawn,
    Square("h2") -> WhitePawn)

  val blackPieces = Map(
    Square("a7") -> BlackPawn,
    Square("b7") -> BlackPawn,
    Square("c7") -> BlackPawn,
    Square("d7") -> BlackPawn,
    Square("e7") -> BlackPawn,
    Square("f7") -> BlackPawn,
    Square("g7") -> BlackPawn,
    Square("h7") -> BlackPawn,

    Square("a8") -> BlackRook,
    Square("b8") -> BlackKnight,
    Square("c8") -> BlackBishop,
    Square("d8") -> BlackQueen,
    Square("e8") -> BlackKing,
    Square("f8") -> BlackBishop,
    Square("g8") -> BlackKnight,
    Square("h8") -> BlackRook)
}

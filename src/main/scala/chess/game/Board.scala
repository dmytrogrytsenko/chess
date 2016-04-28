package chess.game

import chess.common._

case class Board(squares: Map[Square, Piece]) {
  lazy val byColor = Set(White, Black).map(filterByColor).toMap
  lazy val king = Set(White, Black).map(findKing).toMap

  def make(mv: Movement): Board =
    this.move(mv.src -> mv.dst).pipe { updating =>
      mv.kind match {
        case EnPassant => updating.clear(mv.capturedSquare.get)
        case Promotion => updating.put(mv.dst, mv.promoted.get)
        case Castling => updating.move(mv.guardSrc.get, mv.guardDst.get)
        case _ => updating
      }
    }

  private def move(src2dst: (Square, Square)) = this.clear(src2dst._1).put(src2dst._2, squares(src2dst._1))
  private def clear(square: Square) = this.copy(squares = squares - square)
  private def put(square: Square, piece: Piece) = this.copy(squares = squares + (square -> piece))

  private def filterByColor(color: PieceColor) =
    color -> squares.filter { case (_, piece) => piece.color == color }

  private def findKing(color: PieceColor) =
    color -> squares
      .find { case (_, piece) => piece == Piece(color, King) }
      .map { case (square, _) => square }
}

object Board {
  val empty = Board(Map.empty)

  val initial = {
    val kinds = Vector(Rook, Knight, Bishop, Queen, King, Bishop, Knight, Rook)
    def pieces(color: PieceColor) = kinds.map(Piece(color, _))
    val squares = Map.empty[Square, Piece]
      .++(Rank(1).squares.map(square => square -> pieces(White)(square.file.index - 1)))
      .++(Rank(2).squares.map(_ -> Pieces.WhitePawn))
      .++(Rank(7).squares.map(_ -> Pieces.BlackPawn))
      .++(Rank(8).squares.map(square => square -> pieces(Black)(square.file.index - 1)))
    Board(squares)
  }
}

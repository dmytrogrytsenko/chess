package chess.game

case class Board(squares: Map[Square, Piece]) {
  lazy val byColor: Map[PieceColor, Map[Square, Piece]] = Set(White, Black).map(filterByColor).toMap
  lazy val king: Map[PieceColor, Option[Square]] = Set(White, Black).map(findKing).toMap

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

package chess.game

case class Piece(color: PieceColor, kind: PieceKind)

object Pieces {
  val WhiteKing = Piece(White, King)
  val WhiteQueen = Piece(White, Queen)
  val WhiteRook = Piece(White, Rook)
  val WhiteBishop = Piece(White, Bishop)
  val WhiteKnight = Piece(White, Knight)
  val WhitePawn = Piece(White, Pawn)

  val BlackKing = Piece(Black, King)
  val BlackQueen = Piece(Black, Queen)
  val BlackRook = Piece(Black, Rook)
  val BlackBishop = Piece(Black, Bishop)
  val BlackKnight = Piece(Black, Knight)
  val BlackPawn = Piece(Black, Pawn)
}

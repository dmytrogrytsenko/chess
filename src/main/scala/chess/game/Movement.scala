package chess.game

case class Movement(kind: MovementKind,
                    piece: Piece,
                    src: Square,
                    dst: Square,
                    captured: Option[Piece] = None,
                    promoted: Option[Piece] = None,
                    castlingKind: Option[CastlingKind] = None) {
  def capturedSquare = kind match {
    case Capture => Some(dst)
    case EnPassant => Some(Square(dst.file, Rank((src.index + dst.index) / 2)))
    case _ => None
  }

  def guardSrc = castlingKind.map {
    case KingSide => Square(File('h'), src.rank)
    case QueenSide => Square(File('a'), src.rank)
  }

  def guardDst = castlingKind.map {
    case KingSide => Square(File('f'), src.rank)
    case QueenSide => Square(File('c'), src.rank)
  }
}

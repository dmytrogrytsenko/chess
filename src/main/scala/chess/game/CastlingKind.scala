package chess.game

sealed trait CastlingKind {
  def name: String
}

case object KingSide extends CastlingKind {
  val name = "kingside"
}

case object QueenSide extends CastlingKind {
  val name = "queenside"
}

object CastlingKind {
  val all = Set(KingSide, QueenSide)

  def apply(name: String): CastlingKind = all
    .find(_.name == name)
    .getOrElse {
      throw new IllegalArgumentException(s"Invalid castling kind name: $name")
    }
}

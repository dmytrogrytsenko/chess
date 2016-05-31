package chess.game

sealed trait PieceKind {
  def name: Char
}

case object King extends PieceKind {
  val name = 'K
}

case object Queen extends PieceKind {
  val name = 'Q'
}

case object Rook extends PieceKind {
  val name = 'R'
}

case object Bishop extends PieceKind {
  val name = 'B'
}

case object Knight extends PieceKind {
  val name = 'N'
}

case object Pawn extends PieceKind {
  val name = 'P'
}

object PieceKind {
  def apply(name: Char): PieceKind = name match {
    case King.name => King
    case Queen.name => Queen
    case Rook.name => Rook
    case Bishop.name => Bishop
    case Knight.name => Knight
    case Pawn.name => Pawn
    case name => throw new IllegalArgumentException(s"Invalid piece kind name: $name.")
  }
}
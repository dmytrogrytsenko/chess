package chess.game

sealed trait MovementKind {
  def name: String
}

case object Regular extends MovementKind {
  val name = "regular"
}

case object Capture extends MovementKind {
  val name = "capture"
}

case object EnPassant extends MovementKind {
  val name = "enpassant"
}

case object Promotion extends MovementKind {
  val name = "promotion"
}

case object Castling extends MovementKind {
  val name = "castling"
}

object MovementKind {
  val all = Set(Regular, Capture, EnPassant, Promotion, Castling)

  def apply(name: String): MovementKind = all
    .find(_.name == name)
    .getOrElse {
      throw new IllegalArgumentException(s"Invalid movement kind name: $name")
    }
}
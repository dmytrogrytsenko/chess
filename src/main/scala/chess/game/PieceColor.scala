package chess.game

sealed trait PieceColor {
  def name: Char
  def opposite: PieceColor
}

case object White extends PieceColor {
  val name = 'W'
  val opposite = Black
}

case object Black extends PieceColor {
  val name = 'B'
  val opposite = White
}

object PieceColor {
  def all = Set(White, Black)

  def apply(name: Char): PieceColor = all
    .find(_.name == name)
    .getOrElse {
      throw new IllegalArgumentException(s"Invalid piece color name: $name.")
    }
}
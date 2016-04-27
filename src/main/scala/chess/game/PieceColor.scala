package chess.game

sealed trait PieceColor {
  def opposite: PieceColor
}

case object White extends PieceColor {
  val opposite = Black
}

case object Black extends PieceColor {
  val opposite = White
}

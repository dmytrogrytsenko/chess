package chess.game

sealed trait MovementKind

case object Regular extends MovementKind

case object Capture extends MovementKind

case object EnPassant extends MovementKind

case object Promotion extends MovementKind

case object Castling extends MovementKind
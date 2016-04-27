package chess.game

sealed trait CastlingKind

case object KingSide extends CastlingKind
case object QueenSide extends CastlingKind

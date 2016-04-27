package chess.game

case class Movement(kind: MovementKind,
                    piece: Piece,
                    src: Square,
                    dst: Square,
                    captured: Option[Piece] = None,
                    promoted: Option[Piece] = None,
                    castlingKind: Option[CastlingKind] = None)

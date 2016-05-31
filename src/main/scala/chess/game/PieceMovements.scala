package chess.game

object PieceMovements {

  def underAttack(game: Game, square: Square): Boolean =
    validMovements(game, game.movingPlayer.opposite)
      .filter(m => m.kind == Regular || m.kind == Capture)
      .exists(_.dst == square)

  def validMovements(game: Game, color: PieceColor): List[Movement] =
    game.board
      .byColor(color)
      .flatMap { case (square, piece) => squareMovements(game, piece, square) }
      .toList

  private def squareMovements(game: Game, piece: Piece, src: Square): List[Movement] =
    piece.kind match {
      case King => kingMovements(game, piece, src)
      case Queen => radiants(game, piece, src, straightDeltas ::: diagonalDeltas, recursive = true)
      case Bishop => radiants(game, piece, src, diagonalDeltas, recursive = true)
      case Knight => radiants(game, piece, src, knightDeltas, recursive = false)
      case Rook => radiants(game, piece, src, straightDeltas, recursive = true)
      case Pawn => pawnMovements(game, piece, src)
    }

  private val straightDeltas = List((-1, 0), (1, 0), (0, -1), (0, 1))
  private val diagonalDeltas = List((-1, -1), (-1, 1), (1, -1), (1, 1))
  private val knightDeltas = List((-2, -1), (-2, 1), (-1, -2), (-1, 2), (1, -2), (1, 2), (2, -1), (2, -1))

  private val empty = List.empty[Movement]
  private val none = Option.empty[Movement]

  private def kingMovements(game: Game, piece: Piece, src: Square): List[Movement] = {
    val startRankIndex = if (piece.color == White) 1 else 8
    def square(fileName: Char) =  Square(File(fileName), Rank(startRankIndex))

    val kingSideCastling =
      for {
        dst <- Some(square('g'))
        if src == square('e')
        if piece.color == game.movingPlayer
        if piece.kind == King
        if game.initials.contains(square('e'))
        if game.initials.contains(square('h'))
        if game.board.squares.get(square('e')).contains(Piece(piece.color, Rook))
        if game.board.squares.get(square('f')).isEmpty
        if game.board.squares.get(square('g')).isEmpty
        if game.board.squares.get(square('h')).contains(Piece(piece.color, Rook))
        if !underAttack(game, square('e'))
        if !underAttack(game, square('f'))
        if !underAttack(game, square('g'))
      } yield Movement(Castling, piece, src, dst, castlingKind = Some(KingSide))

    val queenSideCastling =
      for {
        dst <- Some(square('b'))
        if src == square('e')
        if piece.color == game.movingPlayer
        if piece.kind == King
        if game.initials.contains(square('e'))
        if game.initials.contains(square('a'))
        if game.board.squares.get(square('e')).contains(Piece(piece.color, Rook))
        if game.board.squares.get(square('d')).isEmpty
        if game.board.squares.get(square('c')).isEmpty
        if game.board.squares.get(square('b')).isEmpty
        if game.board.squares.get(square('a')).contains(Piece(piece.color, Rook))
        if !underAttack(game, square('e'))
        if !underAttack(game, square('d'))
        if !underAttack(game, square('c'))
        if !underAttack(game, square('b'))
      } yield Movement(Castling, piece, src, dst, castlingKind = Some(QueenSide))

    radiants(game, piece, src, straightDeltas ::: diagonalDeltas, recursive = false)
  }

  private def pawnMovements(game: Game, piece: Piece, src: Square): List[Movement] = {
    val (startRankIndex, finishRankIndex, rankDelta) = if (piece.color == White) (2, 8, 1) else (7, 1, -1)

    val first =
      for {
        step <- src.sibling(0, rankDelta)
        if src.rank.index == startRankIndex
        if game.initials.contains(src)
        if !game.board.squares.contains(step)
        dst <- step.sibling(0, rankDelta)
        if !game.board.squares.contains(dst)
      } yield Movement(Regular, piece, src, dst)

    val regular =
      for {
        dst <- src.sibling(0, rankDelta)
        if dst.rank.index != finishRankIndex
        if !game.board.squares.contains(dst)
      } yield Movement(Regular, piece, src, dst)

    def captures =
      for {
        fileDelta <- List(-1, 1)
        dst <- src.sibling(fileDelta, rankDelta)
        captured <- game.board.squares.get(dst)
        if captured.color != piece.color
      } yield Movement(Capture, piece, src, dst, captured = Some(captured))

    val promotions =
      for {
        dst <- src.sibling(0, rankDelta).toList
        if dst.rank.index == finishRankIndex
        if !game.board.squares.contains(dst)
        promotedKind <- Seq(Queen, Bishop, Knight, Rook)
      } yield Movement(Promotion, piece, src, dst, promoted = Some(Piece(piece.color, promotedKind)))

    val enPassants =
      for {
        fileDelta <- List(-1, 1)
        dst <- src.sibling(fileDelta, rankDelta)
        if !game.board.squares.contains(dst)
        previousMovement <- game.history.lastOption
        if previousMovement.kind == Regular
        if previousMovement.piece.color != piece.color
        if previousMovement.piece.kind == Pawn
        if Math.abs(previousMovement.src.rank.index - previousMovement.dst.rank.index) == 2
      } yield Movement(EnPassant, piece, src, dst, captured = Some(previousMovement.piece))

    first.toList ::: regular.toList ::: captures ::: promotions ::: enPassants
  }

  private def radiants(game: Game,
                       piece: Piece,
                       src: Square,
                       deltas: List[(Int, Int)],
                       recursive: Boolean): List[Movement] =
    deltas.foldLeft(empty) { case (acc, (fileDelta, rankDelta)) =>
      acc ::: radiant(game, piece, src, src, fileDelta, rankDelta, recursive)
    }

  private def radiant(game: Game,
                      piece: Piece,
                      src: Square,
                      current: Square,
                      fileDelta: Int,
                      rankDelta: Int,
                      recursive: Boolean): List[Movement] =
    current.sibling(fileDelta, rankDelta).fold(empty) { dst =>
      val movement = game.board.squares.get(dst) match {
        case Some(captured) if captured.color == piece.color => None
        case Some(captured) => Some(Movement(Capture, piece, src, dst, captured = Some(captured)))
        case None => Some(Movement(Regular, piece, src, dst))
      }
      movement.toList ::: (if (recursive) radiant(game, piece, src, dst, fileDelta, rankDelta, recursive) else empty)
    }
}

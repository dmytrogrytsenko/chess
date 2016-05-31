package chess.game

object FenBuilder {
  //"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

  def build(game: Game): String = {
    val pieces = Rank.all.reverse.map(rank => buildRank(game, rank)).mkString("/")
    val color = game.movingPlayer.name.toLower
    val castling = buildCastling(game)
    val enpassant = buildEnpassant(game)
    val halfmoveClock = buildHalfmoveClock(game)
    val fullmoveNumber = game.history.size / 2 + 1
    s"$pieces $color $castling $enpassant $halfmoveClock $fullmoveNumber"
  }

  private def buildRank(game: Game, rank: Rank): String =
    File.all
      .map(file => game.board.squares.get(Square(file, rank)).fold('1')(buildPiece))
      .mkString
      .replace("11111111", "8")
      .replace("1111111", "7")
      .replace("111111", "6")
      .replace("11111", "5")
      .replace("1111", "4")
      .replace("111", "3")
      .replace("11", "2")

  def buildPiece(piece: Piece) =
    piece.color match {
      case White => piece.kind.name.toUpper
      case Black => piece.kind.name.toLower
    }

  private def isInitial(game: Game, square: String*): Boolean = square.map(Square.apply).forall(game.initials.contains)

  private def buildCastling(game: Game): String = {
    val K = if (isInitial(game, "e1", "h1")) "K" else ""
    val Q = if (isInitial(game, "e1", "a1")) "Q" else ""
    val k = if (isInitial(game, "e8", "h8")) "k" else ""
    val q = if (isInitial(game, "e8", "a8")) "q" else ""
    Some(s"$K$Q$k$q").filter(_.nonEmpty).getOrElse("-")
  }

  private def buildEnpassant(game: Game): String =
    (for {
      m <- game.history.lastOption
      if m.kind == Regular
      if m.piece.kind == Pawn
      if Math.abs(m.src.rank.index - m.dst.rank.index) == 2
    } yield Square(m.src.file, Rank((m.src.rank.index + m.dst.rank.index) / 2)).name).getOrElse("-")

  private def buildHalfmoveClock(game: Game): String = {
    def calc(ms: List[Movement]): List[Movement] = ms match {
      case Nil => Nil
      case _ if ms.exists(m => m.piece.kind == Pawn || m.kind == Capture) => calc(ms.tail)
      case _ => ms
    }
    calc(game.history).size.toString
  }

}
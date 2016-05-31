
java.util.UUID.randomUUID().toString

import chess.domain.Identifiers._
import chess.game.{Game, Pieces, Square}

val game: Game = Game.create(UserId.generate, UserId.generate)
val z = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
val a = game.fen

val e2e4 = game.movements.find(_.dst == Square("e4")).get
val game2 = game.make(e2e4)
"rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
game2.fen

val c5 = game2.movements.find(m => m.piece == Pieces.BlackPawn && m.dst == Square("c5")).get
val game3 = game2.make(c5)
"rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2"
game3.fen

val Nf3 = game3.movements.find(m => m.piece == Pieces.WhiteKnight && m.dst == Square("f3")).get
val game4 = game3.make(Nf3)
"rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2"
game4.fen
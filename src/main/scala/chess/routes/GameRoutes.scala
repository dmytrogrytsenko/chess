package chess.routes

import akka.http.scaladsl.model.StatusCodes._
import chess.common.Messages.Done
import chess.controllers.{GetGameController, GetGamesController, MoveController}
import chess.domain.{GameData, GamesData}
import chess.domain.Identifiers.Version
import chess.game.{PieceKind, Square}
import chess.rest.Routes

trait GameRoutes extends Routes {
  val gameRoutes = getGames ~ getGame ~ move

  def getGames =
    path("games") {
      get {
        authenticate(userAuthenticator) { userId =>
          complete {
            GetGamesController.props(userId).execute[GamesData]
          }
        }
      }
    }

  def getGame =
    path("games" / GameIdSegment) { gameId =>
      parameter("version".as[Version] ?) { version =>
        get {
          authenticate(userAuthenticator) { userId => ctx =>
            GetGameController.props(userId, gameId, version).execute[Option[GameData]].flatMap {
              case Some(content) => ctx.complete(OK -> content)
              case None => ctx.complete(NoContent)
            }
          }
        }
      }
    }

  def move =
    path("games" / GameIdSegment / "move") { gameId =>
      parameter("src".as[Square], "dst".as[Square], "promoted".as[PieceKind] ?) { (src, dst, promoted) =>
        post {
          authenticate(userAuthenticator) { userId =>
            complete {
              MoveController.props(userId, gameId, src, dst, promoted).execute[Done]
            }
          }
        }
      }
    }
}

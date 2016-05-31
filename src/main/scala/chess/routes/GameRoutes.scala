package chess.routes

import akka.http.scaladsl.model.StatusCodes._
import chess.controllers.GetGameController
import chess.domain.GameData
import chess.domain.Identifiers.Version
import chess.rest.Routes

trait GameRoutes extends Routes {
  val gameRoutes = getGame

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

}

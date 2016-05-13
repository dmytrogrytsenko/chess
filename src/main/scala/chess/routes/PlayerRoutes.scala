package chess.routes

import akka.http.scaladsl.model.StatusCodes._
import chess.controllers.GetPlayersController
import chess.domain.Identifiers.Version
import chess.domain.PlayersData
import chess.rest.Routes

trait PlayerRoutes extends Routes {
  val playerRoutes = getPlayers

  def getPlayers =
    path("players") {
      parameter("version".as[Option[Version]]) { version =>
        get {
          authenticate(userAuthenticator) { userId => ctx =>
            GetPlayersController.props(userId, version).execute[Option[PlayersData]].flatMap {
              case Some(content) => ctx.complete(OK -> content)
              case None => ctx.complete(NoContent)
            }
          }
        }
      }
    }
}

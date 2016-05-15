package chess.routes

import akka.http.scaladsl.model.StatusCodes._
import chess.common.Messages.Done
import chess.controllers.{InviteController, GetPlayersController}
import chess.domain.Identifiers.{UserId, Version}
import chess.domain.{InvitationData, PlayersData}
import chess.rest.Routes

trait PlayerRoutes extends Routes {
  val playerRoutes = getPlayers ~ invite

  def getPlayers =
    path("players") {
      parameter("version".as[Version] ?) { version =>
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

  def invite =
    path("invite") {
      parameter("player".as[UserId]) { inviteeId =>
        post {
          authenticate(userAuthenticator) { userId =>
            complete {
              InviteController.props(userId, inviteeId).execute[InvitationData]
            }
          }
        }
      }
    }
}

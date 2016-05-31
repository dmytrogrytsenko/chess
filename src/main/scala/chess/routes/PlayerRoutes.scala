package chess.routes

import akka.http.scaladsl.model.StatusCodes._
import chess.controllers._
import chess.domain.Identifiers.{InvitationId, UserId, Version}
import chess.domain.{InvitationData, PlayersData}
import chess.rest.Routes

trait PlayerRoutes extends Routes {
  val playerRoutes = getPlayers ~ invite ~ cancelInvitation ~ rejectInvitation ~ acceptInvitation

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

  def cancelInvitation =
    path("invitations" / "cancel") {
      parameter("id".as[InvitationId]) { invitationId =>
        put {
          authenticate(userAuthenticator) { userId =>
            complete {
              CancelInvitationController.props(userId, invitationId).execute[InvitationData]
            }
          }
        }
      }
    }

  def rejectInvitation =
    path("invitations" / "reject") {
      parameter("id".as[InvitationId]) { invitationId =>
        put {
          authenticate(userAuthenticator) { userId =>
            complete {
              RejectInvitationController.props(userId, invitationId).execute[InvitationData]
            }
          }
        }
      }
    }

  def acceptInvitation =
    path("invitations" / "accept") {
      parameter("id".as[InvitationId]) { invitationId =>
        put {
          authenticate(userAuthenticator) { userId =>
            complete {
              AcceptInvitationController.props(userId, invitationId).execute[InvitationData]
            }
          }
        }
      }
    }

}

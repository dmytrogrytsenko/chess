package chess.repositories

import chess.common._
import chess.common.Messages.Start
import chess.common.actors.{BaseActor, NodeSingleton1}
import chess.domain.Identifiers.{InvitationId, UserId}
import chess.domain.{InvitationStatus, Invitation}
import chess.mongo.InvitationCollection
import reactivemongo.api.DB

object InvitationRepository extends NodeSingleton1[InvitationRepository, DB] {
  case class GetPendingInviters(inviteeId: UserId)
  case class PendingInviters(invitations: List[Invitation])
  case class GetPendingInvitees(inviteeId: UserId)
  case class PendingInvitees(invitations: List[Invitation])
  case class Invite(inviterId: UserId, inviteeId: UserId)
  case class GetInvitation(id: InvitationId)
  case class InvitationNotFound(id: InvitationId)
  case class CompleteInvitation(id: InvitationId, status: InvitationStatus)
  case class InvitationCompleted(invitation: Invitation)
}

class InvitationRepository(implicit val db: DB) extends BaseActor {

  import InvitationCollection._
  import InvitationRepository._
  import context.dispatcher

  def receive = {
    case Start => ensureIndexes

    case GetInvitation(id) =>
      get(id) map {
        case Some(invitation) => invitation
        case None => InvitationNotFound(id)
      } pipeTo sender()

    case GetPendingInvitees(inviterId) =>
      getPendingInvitees(inviterId) map PendingInvitees.apply pipeTo sender()

    case GetPendingInviters(inviteeId) =>
      getPendingInviters(inviteeId) map PendingInviters.apply pipeTo sender()

    case Invite(inviterId, inviteeId) =>
      invite(inviterId, inviteeId) pipeTo sender()

    case CompleteInvitation(id, status) =>
      complete(id, status) map InvitationCompleted.apply pipeTo sender()
  }
}
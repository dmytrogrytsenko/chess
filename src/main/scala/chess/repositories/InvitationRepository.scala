package chess.repositories

import chess.common._
import chess.common.Messages.Start
import chess.common.actors.{BaseActor, NodeSingleton1}
import chess.domain.Identifiers.UserId
import chess.domain.Invitation
import chess.mongo.InvitationCollection
import reactivemongo.api.DB

object InvitationRepository extends NodeSingleton1[InvitationRepository, DB] {
  case class GetPendingInviters(inviteeId: UserId)
  case class PendingInviters(invitations: List[Invitation])
  case class GetPendingInvitees(inviteeId: UserId)
  case class PendingInvitees(invitations: List[Invitation])
  case class Invite(inviterId: UserId, inviteeId: UserId)
}

class InvitationRepository(implicit val db: DB) extends BaseActor {

  import InvitationCollection._
  import InvitationRepository._
  import context.dispatcher

  def receive = {
    case Start => ensureIndexes

    case GetPendingInvitees(inviterId) =>
      getPendingInvitees(inviterId) map PendingInvitees.apply pipeTo sender()

    case GetPendingInviters(inviteeId) =>
      getPendingInviters(inviteeId) map PendingInviters.apply pipeTo sender()

    case Invite(inviterId, inviteeId) =>
      invite(inviterId, inviteeId) pipeTo sender()
  }
}
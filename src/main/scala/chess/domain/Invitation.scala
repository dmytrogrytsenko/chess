package chess.domain

import chess.domain.Identifiers._
import chess.domain.InvitationStatuses.Pending
import org.joda.time.DateTime

sealed trait InvitationStatus {
  def value: String
}

object InvitationStatuses {

  case object Pending extends InvitationStatus {
    val value = "pending"
  }

  case object Canceled extends InvitationStatus {
    val value = "canceled"
  }

  case object Rejected extends InvitationStatus {
    val value = "rejected"
  }

  case object Accepted extends InvitationStatus {
    val value = "accepted"
  }
}

case class Invitation(id: InvitationId,
                      inviterId: UserId,
                      inviteeId: UserId,
                      createdAt: DateTime,
                      status: InvitationStatus,
                      completedAt: Option[DateTime])

object InvitationStatus {
  import InvitationStatuses._
  def all = Set(Pending, Canceled, Rejected, Accepted)
  def parse(value: String): Option[InvitationStatus] = all.find(_.value == value)
}

object Invitation {
  def apply(inviterId: UserId, inviteeId: UserId): Invitation =
    Invitation(
      id = InvitationId.generate(),
      inviterId = inviterId,
      inviteeId = inviteeId,
      createdAt = DateTime.now,
      status = Pending,
      completedAt = None)
}

case class InvitationData(id: InvitationId,
                          inviter: UserData,
                          invitee: UserData,
                          createdAt: DateTime,
                          status: InvitationStatus,
                          completedAt: Option[DateTime])

object InvitationData {
  def apply(invitation: Invitation,
            inviter: User,
            invitee: User): InvitationData =
    InvitationData(
      id = invitation.id,
      inviter = UserData(inviter),
      invitee = UserData(invitee),
      createdAt = invitation.createdAt,
      status = invitation.status,
      completedAt = invitation.completedAt)
}

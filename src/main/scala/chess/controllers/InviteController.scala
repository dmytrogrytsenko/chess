package chess.controllers

import akka.actor.Props
import chess.common.Messages.Start
import chess.domain.Identifiers.UserId
import chess.domain.{User, InvitationData, Invitation}
import chess.repositories.UserRepository.{UserFoundById, UserNotFoundById, FindUserById}
import chess.repositories.{UserRepository, InvitationRepository}
import chess.repositories.InvitationRepository.Invite
import chess.rest.Controller
import chess.rest.Errors._

object InviteController {
  def props(userId: UserId, inviteeId: UserId) =
    Props(classOf[InviteController], userId, inviteeId)
}

class InviteController(userId: UserId, inviteeId: UserId) extends Controller {
  def receive = {
    case Start =>
      InvitationRepository.endpoint ! Invite(userId, inviteeId)
      UserRepository.endpoint ! FindUserById(userId)
      UserRepository.endpoint ! FindUserById(inviteeId)
      wait(Data())
  }

  def wait(data: Data): Unit = {
    if (data.ready) complete(data.result) else become(waiting(data))
  }

  def waiting(data: Data): Receive = {
    case invitation @ Invitation(_, `userId`, `inviteeId`, _, _, _) =>
      wait(data.copy(invitation = Some(invitation)))
    case UserFoundById(user) if user.id == userId =>
      wait(data.copy(inviter = Some(user)))
    case UserFoundById(user) if user.id == inviteeId =>
      wait(data.copy(invitee = Some(user)))
    case _: UserNotFoundById =>
      failure(NotFound.resourceNotFound)
  }

  case class Data(invitation: Option[Invitation] = None,
                  inviter: Option[User] = None,
                  invitee: Option[User] = None) {
    def ready = invitation.nonEmpty && inviter.nonEmpty && invitee.nonEmpty
    def result = InvitationData(invitation.get, inviter.get, invitee.get)
  }
}

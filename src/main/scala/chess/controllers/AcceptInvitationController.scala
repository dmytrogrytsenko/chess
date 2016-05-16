package chess.controllers

import akka.actor.Props
import chess.common.Messages.Start
import chess.domain.Identifiers._
import chess.domain.{InvitationData, User, Invitation}
import chess.domain.InvitationStatuses.{Accepted, Pending}
import chess.repositories.UserRepository.{UserFoundById, UserNotFoundById, FindUserById}
import chess.repositories.{UserRepository, InvitationRepository}
import chess.repositories.InvitationRepository._
import chess.rest.Controller
import chess.rest.Errors.{Conflict, Forbidden, NotFound}

object AcceptInvitationController {
  def props(userId: UserId, invitationId: InvitationId) =
    Props(classOf[AcceptInvitationController], userId, invitationId)
}

class AcceptInvitationController(userId: UserId, invitationId: InvitationId) extends Controller {
  def receive = {
    case Start =>
      InvitationRepository.endpoint ! GetInvitation(invitationId)
      become(waitingForInvitation)
  }

  def waitingForInvitation: Receive = {
    case InvitationNotFound(`invitationId`) => failure(NotFound.invitationNotFound)
    case invitation: Invitation if invitation.inviteeId != userId => failure(Forbidden.accessDenied)
    case invitation: Invitation if invitation.status != Pending => failure(Conflict.incorrectInvitationStatus)
    case invitation: Invitation =>
      UserRepository.endpoint ! FindUserById(invitation.inviterId)
      become(waitingForInviter(invitation))
  }

  def waitingForInviter(invitation: Invitation): Receive = {
    case _: UserNotFoundById => failure(NotFound.userNotFound)
    case UserFoundById(inviter) =>
      UserRepository.endpoint ! FindUserById(invitation.inviteeId)
      become(waitingForInvitee(inviter))
  }

  def waitingForInvitee(inviter: User): Receive = {
    case _: UserNotFoundById => failure(NotFound.userNotFound)
    case UserFoundById(invitee) =>
      InvitationRepository.endpoint ! CompleteInvitation(invitationId, Accepted)
      become(waitingForCompleted(inviter, invitee))
  }

  def waitingForCompleted(inviter: User, invitee: User): Receive = {
    case InvitationCompleted(result) => complete(InvitationData(result, inviter, invitee))
  }
}

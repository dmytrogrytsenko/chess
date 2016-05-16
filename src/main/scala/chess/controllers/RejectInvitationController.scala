package chess.controllers

import akka.actor.Props
import chess.common.Messages.Start
import chess.domain.Identifiers._
import chess.domain.{InvitationData, User, Invitation}
import chess.domain.InvitationStatuses.{Rejected, Pending}
import chess.mongo.VersionCollection._
import chess.repositories.UserRepository.{UserFoundById, UserNotFoundById, FindUserById}
import chess.repositories.VersionRepository.{IncrementVersion, VersionIncremented}
import chess.repositories.{VersionRepository, UserRepository, InvitationRepository}
import chess.repositories.InvitationRepository._
import chess.rest.Controller
import chess.rest.Errors.{Conflict, Forbidden, NotFound}

object RejectInvitationController {
  def props(userId: UserId, invitationId: InvitationId) =
    Props(classOf[RejectInvitationController], userId, invitationId)
}

class RejectInvitationController(userId: UserId, invitationId: InvitationId) extends Controller {
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
      InvitationRepository.endpoint ! CompleteInvitation(invitationId, Rejected)
      become(waitingForCompleted(inviter, invitee))
  }

  def waitingForCompleted(inviter: User, invitee: User): Receive = {
    case InvitationCompleted(invitation) =>
      VersionRepository.endpoint ! IncrementVersion(players)
      become(waitingForVersionIncremented(invitation, inviter, invitee))
  }

  def waitingForVersionIncremented(invitation: Invitation, inviter: User, invitee: User): Receive = {
    case VersionIncremented(`players`, _) => complete(InvitationData(invitation, inviter, invitee))
  }

}

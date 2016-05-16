package chess.controllers

import akka.actor.Props
import chess.common.Messages.Start
import chess.domain.Identifiers._
import chess.domain._
import chess.mongo.VersionCollection._
import chess.repositories.InvitationRepository.{GetPendingInvitees, GetPendingInviters, PendingInvitees, PendingInviters}
import chess.repositories.{InvitationRepository, SessionRepository, UserRepository, VersionRepository}
import chess.repositories.SessionRepository.{GetOnlineSessions, OnlineSessions}
import chess.repositories.UserRepository.{GetUsers, RetrievedUsers}
import chess.repositories.VersionRepository._
import chess.rest.Controller
import chess.settings.AppSettings

object GetPlayersController {
  def props(userId: UserId, version: Option[Version]) =
    Props(classOf[GetPlayersController], userId, version)
}

class GetPlayersController(userId: UserId, version: Option[Version]) extends Controller {
  val settings = AppSettings(context.system.settings.config)

  def receive = {
    case Start =>
      VersionRepository.endpoint ! GetVersion(players)

    case RetrievedVersion(`players`, currentVersion) if version.contains(currentVersion) =>
      complete(Option.empty[PlayersData])

    case RetrievedVersion(`players`, currentVersion) =>
      SessionRepository.endpoint ! GetOnlineSessions(settings.chess.onlineUserTimeout)
      InvitationRepository.endpoint ! GetPendingInviters(userId)
      InvitationRepository.endpoint ! GetPendingInvitees(userId)
      waitForData(Data(currentVersion))
  }

  def waitForData(data: Data): Unit = {
    if (data.ready) {
      UserRepository.endpoint ! GetUsers(data.userIds)
      become(waitingForUsers(data))
    } else {
      become(waitingForData(data))
    }
  }

  def waitingForData(data: Data): Receive = {
    case OnlineSessions(sessions) => waitForData(data.copy(sessions = Some(sessions)))
    case PendingInviters(invitations) => waitForData(data.copy(inviters = Some(invitations)))
    case PendingInvitees(invitations) => waitForData(data.copy(invitees = Some(invitations)))
  }

  def waitingForUsers(data: Data): Receive = {
    case RetrievedUsers(users) => complete(Some(data.copy(users = users).result))
  }

  case class Data(currentVersion: Version,
                  sessions: Option[List[Session]] = None,
                  inviters: Option[List[Invitation]] = None,
                  invitees: Option[List[Invitation]] = None,
                  users: List[User] = Nil) {
    def ready = sessions.nonEmpty && inviters.nonEmpty && invitees.nonEmpty

    def userIds =
      sessions.get.map(_.userId).toSet ++
      inviters.get.map(_.inviterId).toSet ++
      invitees.get.map(_.inviteeId).toSet

    def user(userId: UserId) = users.find(_.id == userId).getOrElse(User.unknown(userId))
    def userData(userId: UserId) = UserData(user(userId))
    def onlinePlayers = sessions.get.map(_.userId).filter(_ != userId).map(userData)
    def invitationData(inv: Invitation) = InvitationData(inv, user(inv.inviterId), user(inv.inviteeId))
    def invitations(invs: Option[List[Invitation]]) = invs.get.map(invitationData)

    def result = PlayersData(onlinePlayers,
      invitations(inviters),
      invitations(invitees),
      version = currentVersion)
  }
}

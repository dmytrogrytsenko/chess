package chess.controllers

import akka.actor.Props
import chess.common.Messages.Start
import chess.domain.Identifiers._
import chess.domain._
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

    case RetrievedVersion(`players`, currentVersion) if currentVersion == version =>
      complete(Option.empty[PlayersData])

    case RetrievedVersion(`players`, currentVersion) =>
      SessionRepository.endpoint ! GetOnlineSessions(settings.chess.onlineUserTimeout)
      become(waitingForSessions(currentVersion))
  }

  def waitingForSessions(currentVersion: Version): Receive = {
    case OnlineSessions(sessions) if sessions.isEmpty =>
      complete(Some(PlayersData(Nil, currentVersion)))

    case OnlineSessions(sessions) =>
      UserRepository.endpoint ! GetUsers(sessions.map(_.userId).toSet)
      InvitationRepository.endpoint ! GetPendingInviters(userId)
      InvitationRepository.endpoint ! GetPendingInvitees(userId)
      waitForUsers(Data(currentVersion, sessions))
  }

  def waitingForUsers(data: Data): Receive = {
    case RetrievedUsers(users) => waitForUsers(data.copy(users = Some(users)))
    case PendingInviters(invitations) => waitForUsers(data.copy(inviters = Some(invitations)))
    case PendingInvitees(invitations) => waitForUsers(data.copy(invitees = Some(invitations)))
  }

  def waitForUsers(data: Data) = {
    if (data.ready) complete(data.result) else become(waitingForUsers(data))
  }

  case class Data(currentVersion: Version,
                  sessions: List[Session],
                  users: Option[List[User]] = None,
                  inviters: Option[List[Invitation]] = None,
                  invitees: Option[List[Invitation]] = None) {
    def ready = users.isDefined && inviters.isDefined && invitees.isDefined

    def result = Some(PlayersData(sessions.map(buildPlayerData), currentVersion))

    def buildPlayerData(session: Session) = PlayerData(
      user = users.get.find(_.id == session.userId).map(UserData.apply).getOrElse(UserData.unknown(session.userId)),
      userInvitesMe = inviters.get.exists(_.inviteeId == userId),
      userIsInvitedByMe = invitees.get.exists(_.inviterId == userId))
  }
}

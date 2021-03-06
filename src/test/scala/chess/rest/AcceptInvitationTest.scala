package chess.rest

import chess.TestBase
import chess.domain.Identifiers._
import chess.domain.InvitationData
import chess.domain.InvitationStatuses.{Accepted, Canceled, Pending, Rejected}
import chess.game.{Board, White}
import chess.rest.Errors.{Conflict, Forbidden, NotFound, Unauthorized}
import org.joda.time.DateTime

import scala.concurrent.duration._

class AcceptInvitationTest extends TestBase {

  behavior of "PUT /invitations/accept"

  it should "accept invitation correctly" in {
    //arrange
    val user, invitee = Mongo.addUser()
    val session = Mongo.addSession(userId = invitee.id)
    val invitation = Mongo.addInvitation(inviterId = user.id, inviteeId = invitee.id, status = Pending)
    //act
    val result = Rest.acceptInvitation(session.token, invitation.id).to[InvitationData]
    //assert
    val expected = invitation.copy(status = Accepted, completedAt = result.completedAt, gameId = result.gameId)
    result shouldBe InvitationData(expected, user, invitee)
    result.completedAt.get shouldBeInRange DateTime.now +- 2.seconds
    result.gameId should not be None
    //cleanup
    Mongo.removeUsers(user, invitee)
    Mongo.removeSessions(session)
    Mongo.removeInvitations(invitation)
    Mongo.removeGame(result.gameId.get)
  }

  it should "increment players version" in {
    //arrange
    val user, invitee = Mongo.addUser()
    val session = Mongo.addSession(userId = invitee.id)
    val invitation = Mongo.addInvitation(inviterId = user.id, inviteeId = invitee.id, status = Pending)
    val version = Mongo.getPlayersVersion
    //act
    val result = Rest.acceptInvitation(session.token, invitation.id).to[InvitationData]
    //assert
    Mongo.getPlayersVersion should be >= version.next
    //cleanup
    Mongo.removeUsers(user, invitee)
    Mongo.removeSessions(session)
    Mongo.removeInvitations(invitation)
    Mongo.removeGame(result.gameId.get)
  }

  it should "create new game" in {
    //arrange
    val user, invitee = Mongo.addUser()
    val session = Mongo.addSession(userId = invitee.id)
    val invitation = Mongo.addInvitation(inviterId = user.id, inviteeId = invitee.id, status = Pending)
    val version = Mongo.getPlayersVersion
    //act
    val result = Rest.acceptInvitation(session.token, invitation.id).to[InvitationData]
    //assert
    val stored = Mongo.getGame(result.gameId.get).get
    stored.id shouldBe result.gameId.get
    stored.version shouldBe Version.initial
    stored.whitePlayerId shouldBe user.id
    stored.blackPlayerId shouldBe invitee.id
    stored.startTime shouldBeInRange DateTime.now +- 2.seconds
    stored.board shouldBe Board.initial
    stored.movingPlayer shouldBe White
    stored.initials shouldBe Board.initial.squares.keySet
    stored.history.size shouldBe 0
    //cleanup
    Mongo.removeUsers(user, invitee)
    Mongo.removeSessions(session)
    Mongo.removeInvitations(invitation)
    Mongo.removeGame(result.gameId.get)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if token is incorrect" in {
    //act
    val result = Rest.acceptInvitation(Token.generate(), InvitationId.generate()).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
  }

  it should "return 403 (Forbidden) ACCESS_DENIED if user is not invitee" in {
    //arrange
    val user, invitee = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val invitation = Mongo.addInvitation(inviterId = user.id, inviteeId = invitee.id, status = Pending)
    //act
    val result = Rest.acceptInvitation(session.token, invitation.id).toErrorResult
    //assert
    result should be (Forbidden.accessDenied)
    //cleanup
    Mongo.removeUsers(user, invitee)
    Mongo.removeSessions(session)
    Mongo.removeInvitations(invitation)
  }

  it should "return 404 (Not Found) INVITATION_NOT_FOUND if invitation not found" in {
    //arrange
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    //act
    val result = Rest.acceptInvitation(session.token, InvitationId.generate()).toErrorResult
    //assert
    result should be (NotFound.invitationNotFound)
    //cleanup
    Mongo.removeUsers(user)
    Mongo.removeSessions(session)
  }

  it should "return 409 (Conflict) INCORRECT_INVITATION_STATUS if invitation status is not Pending" in {
    //arrange
    val user, invitee = Mongo.addUser()
    val session = Mongo.addSession(userId = invitee.id)
    val invitation = Mongo.addInvitation(inviterId = user.id, inviteeId = invitee.id, status = Accepted)
    //act
    val result = Rest.acceptInvitation(session.token, invitation.id).toErrorResult
    //assert
    result should be (Conflict.incorrectInvitationStatus)
    //cleanup
    Mongo.removeUsers(user, invitee)
    Mongo.removeSessions(session)
    Mongo.removeInvitations(invitation)
  }
}

package chess.rest

import chess.TestBase
import chess.domain.Identifiers.{Token, InvitationId}
import chess.domain.InvitationData
import chess.domain.InvitationStatuses.{Rejected, Accepted, Pending, Canceled}
import chess.rest.Errors.{Conflict, Forbidden, NotFound, Unauthorized}
import org.joda.time.DateTime

import scala.concurrent.duration._

class RejectInvitationTest extends TestBase {

  behavior of "PUT /invitations/reject"

  it should "reject invitation correctly" in {
    //arrange
    val user, invitee = Mongo.addUser()
    val session = Mongo.addSession(userId = invitee.id)
    val invitation = Mongo.addInvitation(inviterId = user.id, inviteeId = invitee.id, status = Pending)
    //act
    val result = Rest.rejectInvitation(session.token, invitation.id).to[InvitationData]
    //assert
    val expected = invitation.copy(status = Rejected, completedAt = result.completedAt)
    result shouldBe InvitationData(expected, user, invitee)
    result.completedAt.get shouldBeInRange DateTime.now +- 2.seconds
    //cleanup
    Mongo.removeUsers(user, invitee)
    Mongo.removeSessions(session)
    Mongo.removeInvitations(invitation)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if token is incorrect" in {
    //act
    val result = Rest.rejectInvitation(Token.generate(), InvitationId.generate()).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
  }

  it should "return 403 (Forbidden) ACCESS_DENIED if user is not invitee" in {
    //arrange
    val user, invitee = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val invitation = Mongo.addInvitation(inviterId = user.id, inviteeId = invitee.id, status = Pending)
    //act
    val result = Rest.rejectInvitation(session.token, invitation.id).toErrorResult
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
    val result = Rest.rejectInvitation(session.token, InvitationId.generate()).toErrorResult
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
    val result = Rest.rejectInvitation(session.token, invitation.id).toErrorResult
    //assert
    result should be (Conflict.incorrectInvitationStatus)
    //cleanup
    Mongo.removeUsers(user, invitee)
    Mongo.removeSessions(session)
    Mongo.removeInvitations(invitation)
  }
}

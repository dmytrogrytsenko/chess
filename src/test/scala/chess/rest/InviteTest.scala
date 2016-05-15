package chess.rest

import chess.TestBase
import chess.domain.Identifiers.{UserId, Token}
import chess.domain.InvitationStatuses.Pending
import chess.domain.{UserData, InvitationData}
import chess.rest.Errors.{NotFound, Unauthorized}
import org.joda.time.DateTime

import scala.concurrent.duration._

class InviteTest extends TestBase {

  behavior of "POST /invite"

  it should "invite player correctly" in {
    //arrange
    val user, invitee = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    //act
    val result = Rest.invite(session.token, invitee.id).to[InvitationData]
    //assert
    result.id should not be empty
    result.inviter shouldBe UserData(user)
    result.invitee shouldBe UserData(invitee)
    result.createdAt shouldBeInRange DateTime.now +- 2.seconds
    result.status shouldBe Pending
    result.completedAt shouldBe None
    val stored = Mongo.getInvitation(result.id).get
    stored.inviterId shouldBe user.id
    stored.inviteeId shouldBe invitee.id
    stored.createdAt shouldBeInRange DateTime.now +- 2.seconds
    stored.status shouldBe Pending
    stored.completedAt shouldBe None
    //cleanup
    Mongo.removeUsers(user, invitee)
    Mongo.removeSessions(session)
    Mongo.removeInvitations(stored)
  }

  it should "do nothing if player already invited" in {
    //arrange
    val user, invitee = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    val invitation = Mongo.addInvitation(inviterId = user.id, inviteeId = invitee.id, status = Pending)
    //act
    val result = Rest.invite(session.token, invitee.id).to[InvitationData]
    //assert
    result shouldBe InvitationData(invitation, user, invitee)
    //cleanup
    Mongo.removeUsers(user, invitee)
    Mongo.removeSessions(session)
    Mongo.removeInvitations(invitation)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if token is incorrect" in {
    //act
    val result = Rest.invite(Token.generate(), UserId.generate()).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
  }

  it should "return 404 (NotFound) USER_NOT_FOUND inviting user not found" in {
    //arrange
    val user, invitee = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    //act
    val result = Rest.invite(session.token, UserId.generate()).toErrorResult
    //assert
    result should be (NotFound.userNotFound)
    //cleanup
    Mongo.removeUsers(user)
    Mongo.removeSessions(session)
  }
}

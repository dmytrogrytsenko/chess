package chess.mongo

import chess.TestBase
import chess.common._
import chess.domain.Identifiers.{GameId, InvitationId, UserId}
import chess.domain.InvitationStatus
import chess.domain.InvitationStatuses.{Accepted, Pending}
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class InvitationCollectionTest extends TestBase {

  import InvitationCollection._

  behavior of "InvitationIdReader and InvitationIdWriter"

  it should "read and write InvitationId correctly" in {
    //arrange
    val invitationId = InvitationId.generate()
    //act
    val result = InvitationIdReader.read(InvitationIdWriter.write(invitationId))
    //assert
    result shouldBe invitationId
  }

  behavior of "InvitationStatusReader and InvitationStatusWriter"

  it should "read and write InvitationStatus correctly" in {
    //arrange
    val status = InvitationStatus.all.randomValue
    //act
    val result = InvitationStatusReader.read(InvitationStatusWriter.write(status))
    //assert
    result shouldBe status
  }

  behavior of "InvitationReader and InvitationWriter"

  it should "read and write Invitation correctly" in {
    //arrange
    val invitation = buildInvitation()
    //act
    val result = InvitationReader.read(InvitationWriter.write(invitation))
    //assert
    result shouldBe invitation
  }

  behavior of "getPendingInviters"

  it should "return pending inviters" in {
    //assert
    val userId, userId2, userId3 = UserId.generate()
    val invitation1 = Mongo.addInvitation(inviterId = userId2, inviteeId = userId, status = Pending)
    val invitation2 = Mongo.addInvitation(inviterId = userId3, inviteeId = userId, status = Accepted)
    val invitation3 = Mongo.addInvitation(inviterId = userId2, inviteeId = userId3, status = Pending)
    //act
    val result = getPendingInviters(userId).await
    //assert
    result should contain.theSameElementsAs (List(invitation1))
    //cleanup
    Mongo.removeInvitations(invitation1, invitation2, invitation3)
  }

  behavior of "getPendingInvitees"

  it should "return pending invitees" in {
    //assert
    val userId, userId2, userId3 = UserId.generate()
    val invitation1 = Mongo.addInvitation(inviterId = userId, inviteeId = userId2, status = Pending)
    val invitation2 = Mongo.addInvitation(inviterId = userId, inviteeId = userId3, status = Accepted)
    val invitation3 = Mongo.addInvitation(inviterId = userId3, inviteeId = userId2, status = Pending)
    //act
    val result = getPendingInvitees(userId).await
    //assert
    result should contain.theSameElementsAs (List(invitation1))
    //cleanup
    Mongo.removeInvitations(invitation1, invitation2, invitation3)
  }

  behavior of "getPendingInvitation"

  it should "return pending invitation" in {
    //assert
    val inviterId, inviteeId = UserId.generate()
    val invitation = Mongo.addInvitation(inviterId = inviterId, inviteeId = inviteeId, status = Pending)
    //act
    val result = getPendingInvitation(inviterId, inviteeId).await
    //assert
    result shouldBe Some(invitation)
    //cleanup
    Mongo.removeInvitations(invitation)
  }

  it should "return None if no pending invitation found" in {
    //act
    val result = getPendingInvitation(UserId.generate(), UserId.generate()).await
    //assert
    result shouldBe None
  }

  behavior of "invite"

  it should "add new invitation" in {
    //arrange
    val inviterId, inviteeId = UserId.generate()
    //act
    val result = invite(inviterId, inviteeId).await
    //assert
    result.id should not be empty
    result.inviterId shouldBe inviterId
    result.inviteeId shouldBe inviteeId
    result.createdAt shouldBeInRange DateTime.now +- 2.seconds
    result.status shouldBe Pending
    result.completedAt shouldBe None
    //cleanup
    Mongo.removeInvitations(result)
  }

  it should "do nothing if pending invitation already existed" in {
    //arrange
    val inviterId, inviteeId = UserId.generate()
    val invitation = invite(inviterId, inviteeId).await
    //act
    val result = invite(inviterId, inviteeId).await
    //assert
    result shouldBe invitation
    //cleanup
    Mongo.removeInvitations(invitation)
  }

  behavior of "complete"

  it should "complete invitation" in {
    //arrange
    val invitation = Mongo.addInvitation()
    val status = (InvitationStatus.all - Pending).randomValue
    val gameId = GameId.generate()
    //act
    val result = complete(invitation.id, status, Some(gameId)).await
    //assert
    result.id shouldBe invitation.id
    result.inviterId shouldBe invitation.inviterId
    result.inviteeId shouldBe invitation.inviteeId
    result.createdAt shouldBe invitation.createdAt
    result.status shouldBe status
    result.completedAt.get shouldBeInRange DateTime.now +- 2.seconds
    result.gameId shouldBe Some(gameId)
    //cleanup
    Mongo.removeInvitations(invitation)
  }

}

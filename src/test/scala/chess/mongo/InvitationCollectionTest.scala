package chess.mongo

import chess.TestBase
import chess.common._
import chess.domain.Identifiers.InvitationId
import chess.domain.InvitationStatus
import chess.domain.InvitationStatuses.{Accepted, Pending}

import scala.concurrent.ExecutionContext.Implicits.global

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
    val user, user2, user3 = Mongo.addUser()
    val invitation1 = Mongo.addInvitation(inviterId = user2.id, inviteeId = user.id, status = Pending)
    val invitation2 = Mongo.addInvitation(inviterId = user3.id, inviteeId = user.id, status = Accepted)
    val invitation3 = Mongo.addInvitation(inviterId = user2.id, inviteeId = user3.id, status = Pending)
    //act
    val result = getPendingInviters(user.id).await
    //assert
    result should contain.theSameElementsAs (List(invitation1))
    //cleanup
    List(user, user2, user3).map(_.id).foreach(Mongo.removeUser)
    List(invitation1, invitation2, invitation3).map(_.id).foreach(Mongo.removeInvitation)
  }

  behavior of "invite"

  it should "add new invitation" in {
    pending
  }

  it should "do nothing if pending invitation already existed" in {
    pending
  }
}

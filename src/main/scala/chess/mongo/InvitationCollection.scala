package chess.mongo

import chess.common.mongo.MongoCollection
import chess.domain.Identifiers._
import chess.domain.{Invitation, InvitationStatus}
import chess.domain.InvitationStatuses._
import org.joda.time.DateTime
import reactivemongo.api.DB
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONReader, BSONString, BSONWriter}
import reactivemongo.extensions.dao.Handlers._

import scala.concurrent.{ExecutionContext, Future}

object InvitationCollection extends MongoCollection[InvitationId, Invitation] {

  val name = "invitations"

  import UserCollection.{UserIdReader, UserIdWriter}

  implicit object InvitationIdReader extends BSONReader[BSONString, InvitationId] {
    def read(bson: BSONString): InvitationId = bson.value.toInvitationId
  }

  implicit object InvitationIdWriter extends BSONWriter[InvitationId, BSONString] {
    def write(value: InvitationId): BSONString = BSONString(value)
  }

  implicit object InvitationStatusReader extends BSONReader[BSONString, InvitationStatus] {
    def read(bson: BSONString): InvitationStatus =
      InvitationStatus.parse(bson.value) getOrElse {
        throw new RuntimeException(s"Invalid InvitationStatus ${bson.value}")
      }
  }

  implicit object InvitationStatusWriter extends BSONWriter[InvitationStatus, BSONString] {
    def write(status: InvitationStatus): BSONString = BSONString(status.value)
  }

  implicit object InvitationWriter extends BSONDocumentWriter[Invitation] {
    def write(value: Invitation) = $doc(
      "_id" -> value.id,
      "inviterId" -> value.inviterId,
      "inviteeId" -> value.inviteeId,
      "createdAt" -> value.createdAt,
      "status" -> value.status,
      "completedAt" -> value.completedAt)
  }

  implicit object InvitationReader extends BSONDocumentReader[Invitation] {
    def read(doc: BSONDocument) = Invitation(
      id = doc.getAs[InvitationId]("_id").get,
      inviterId = doc.getAs[UserId]("inviterId").get,
      inviteeId = doc.getAs[UserId]("inviteeId").get,
      createdAt = doc.getAs[DateTime]("createdAt").get,
      status = doc.getAs[InvitationStatus]("status").get,
      completedAt = doc.getAs[DateTime]("completedAt"))
  }

  def getPendingInviters(inviteeId: UserId)(implicit db: DB, ec: ExecutionContext): Future[List[Invitation]] =
    items
      .find($doc("inviteeId" -> inviteeId, "status" -> Pending.value))
      .cursor[Invitation]()
      .collect[List]()

  def getPendingInvitees(inviterId: UserId)(implicit db: DB, ec: ExecutionContext): Future[List[Invitation]] =
    items
      .find($doc("inviterId" -> inviterId, "status" -> Pending.value))
      .cursor[Invitation]()
      .collect[List]()

  def invite(inviterId: UserId, inviteeId: UserId)(implicit db: DB, ec: ExecutionContext): Future[Invitation] =
    items.findAndUpdate(
      selector = $doc("inviterId" -> inviterId, "inviteeId" -> inviteeId, "status" -> Pending.value),
      update = Invitation(inviterId, inviteeId),
      fetchNewObject = true,
      upsert = true).map(_.result[Invitation].get)
}

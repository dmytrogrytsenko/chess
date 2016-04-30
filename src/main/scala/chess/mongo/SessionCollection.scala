package chess.mongo

import chess.common.mongo.MongoCollection
import chess.domain.Identifiers._
import chess.domain.Session
import org.joda.time.DateTime
import reactivemongo.api.DB
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson._
import reactivemongo.extensions.dao.Handlers._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

object SessionCollection extends MongoCollection[String, Session] {

  val name = "sessions"

  import UserCollection.{UserIdReader, UserIdWriter}

  implicit object TokenReader extends BSONReader[BSONString, Token] {
    def read(bson: BSONString): Token = bson.value.toToken
  }

  implicit object TokenWriter extends BSONWriter[Token, BSONString] {
    def write(value: Token): BSONString = BSONString(value)
  }

  implicit object SessionWriter extends BSONDocumentWriter[Session] {
    def write(value: Session) = $doc(
      "_id" -> value.token,
      "userId" -> value.userId,
      "createdAt" -> value.createdAt,
      "lastActivityAt" -> value.lastActivityAt)
  }

  implicit object SessionReader extends BSONDocumentReader[Session] {
    def read(doc: BSONDocument) = Session(
      token = doc.getAs[Token]("_id").get,
      userId = doc.getAs[UserId]("userId").get,
      createdAt = doc.getAs[DateTime]("createdAt").get,
      lastActivityAt = doc.getAs[DateTime]("lastActivityAt").get)
  }

  override def ensureIndexes(implicit db: DB, ec: ExecutionContext) =
    Future.sequence(List(
      Index(Seq("lastActivityAt" -> IndexType.Descending), options = $doc("expireAfterSeconds" -> 1.hour.toSeconds))
    ) map items.indexesManager.ensure).map(_ => {})

  def activity(token: Token)(implicit db: DB, ec: ExecutionContext): Future[Unit] =
    items.update($id(token), $set("lastActivityAt" -> DateTime.now)).map(_ => { }).recover { case e => println(e) }
}

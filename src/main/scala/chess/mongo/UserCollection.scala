package chess.mongo

import chess.common.mongo.MongoCollection
import chess.domain.Identifiers._
import chess.domain.User
import org.joda.time.DateTime
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.api.DB
import reactivemongo.bson._
import reactivemongo.extensions.dao.Handlers.BSONDateTimeHandler

import scala.concurrent.{ExecutionContext, Future}

object UserCollection extends MongoCollection[UserId, User] {

  val name = "users"

  implicit object UserIdReader extends BSONReader[BSONString, UserId] {
    def read(bson: BSONString): UserId = bson.value.toUserId
  }

  implicit object UserIdWriter extends BSONWriter[UserId, BSONString] {
    def write(value: UserId): BSONString = BSONString(value)
  }

  implicit object UserWriter extends BSONDocumentWriter[User] {
    def write(value: User) = $doc(
      "_id" -> value.id,
      "name" -> value.name,
      "nameLC" -> value.name.toLowerCase,
      "password" -> value.password,
      "displayName" -> value.displayName,
      "createdAt" -> value.createdAt)
  }

  implicit object UserReader extends BSONDocumentReader[User] {
    def read(doc: BSONDocument) = User(
      id = doc.getAs[UserId]("_id").get,
      name = doc.getAs[String]("name").get,
      password = doc.getAs[String]("password").get,
      displayName = doc.getAs[String]("displayName"),
      createdAt = doc.getAs[DateTime]("createdAt").get)
  }

  override def ensureIndexes(implicit db: DB, ec: ExecutionContext) =
    Future.sequence(List(
      Index(Seq("nameLC" -> IndexType.Ascending), unique = true)
    ) map items.indexesManager.ensure).map(_ => {})

  def findUserByName(name: String)(implicit db: DB, ec: ExecutionContext): Future[Option[User]] =
    items.find($doc("nameLC" -> name.toLowerCase)).one[User]

  def getUsers(userIds: Set[UserId])(implicit db: DB, ec: ExecutionContext): Future[List[User]] =
    items.find($doc("_id" $in (userIds.toSeq: _*))).cursor[User].collect[List]()
}

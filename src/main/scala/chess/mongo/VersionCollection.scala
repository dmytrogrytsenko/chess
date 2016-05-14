package chess.mongo

import chess.common.mongo.MongoCollection
import chess.domain.Identifiers._
import chess.domain.VersionItem
import reactivemongo.api.DB
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONInteger, BSONReader, BSONWriter}

import scala.concurrent.{ExecutionContext, Future}

object VersionCollection extends MongoCollection[String, VersionItem] {

  val name = "versions"

  val players = "players"

  implicit object VersionReader extends BSONReader[BSONInteger, Version] {
    def read(bson: BSONInteger): Version = bson.value.toVersion
  }

  implicit object VersionWriter extends BSONWriter[Version, BSONInteger] {
    def write(value: Version): BSONInteger = BSONInteger(value)
  }

  implicit object VersionItemWriter extends BSONDocumentWriter[VersionItem] {
    def write(value: VersionItem) = $doc(
      "_id" -> value.name,
      "version" -> value.version)
  }

  implicit object VersionItemReader extends BSONDocumentReader[VersionItem] {
    def read(doc: BSONDocument) = VersionItem(
      name = doc.getAs[String]("_id").get,
      version = doc.getAs[Version]("version").get)
  }

  def getVersion(name: String)(implicit db: DB, ec: ExecutionContext): Future[Version] =
    items.find($id(name)).one[VersionItem].map(_.map(_.version).getOrElse(Version.initial))
}


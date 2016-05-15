package chess.common.mongo

import reactivemongo.api.DB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.GetLastError._
import reactivemongo.bson._
import reactivemongo.extensions.dsl.BsonDsl

import scala.concurrent.{Future, ExecutionContext}

trait MongoCollection[Key, Entity] extends BsonDsl {

  val writeConcern = Acknowledged

  def name: String

  def ensureIndexes(implicit db: DB, executionContext: ExecutionContext): Future[Unit] = Future { }

  def items(implicit db: DB) = db[BSONCollection](name)

  def all(implicit db: DB,
          reader: BSONDocumentReader[Entity],
          executionContext: ExecutionContext): Future[List[Entity]] =
    items.find($empty).cursor[Entity]().collect[List]()

  def get(id: Key)
         (implicit db: DB,
          identityWriter: BSONWriter[Key, BSONString],
          documentReader: BSONDocumentReader[Entity],
          executionContext: ExecutionContext): Future[Option[Entity]] =
    items.find($id(id)).one[Entity]

  def add(entity: Entity)
         (implicit db: DB,
          writer: BSONDocumentWriter[Entity],
          executionContext: ExecutionContext): Future[Unit] =
    items.insert(entity, writeConcern).map(_ => { })

  def update(id: Key, entity: Entity)
         (implicit db: DB,
          identityWriter: BSONWriter[Key, BSONString],
          writer: BSONDocumentWriter[Entity],
          executionContext: ExecutionContext): Future[Unit] =
    items.update($id(id), entity, writeConcern).map(_ => { })

  def remove(id: Key)
            (implicit db: DB,
             identityWriter: BSONWriter[Key, BSONString],
             executionContext: ExecutionContext): Future[Unit] =
    items.remove($id(id), writeConcern).map(_ => { })
}
